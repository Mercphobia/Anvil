package com.vibe.forge.compiler

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * MODE_A build pipeline: aapt2 -> ecj -> d8 -> apksigner -> install intent.
 * Java-only sources, no external dependencies, per blueprint constraints.
 * All steps run on Dispatchers.IO with structured log streaming.
 */
object BuildPipelineManager {

    data class BuildError(
        val file: String,
        val line: Int,
        val message: String
    )

    data class BuildResult(
        val success: Boolean,
        val apk: File?,
        val errors: List<BuildError>,
        val log: String
    )

    suspend fun build(
        context: Context,
        projectDir: File,
        androidJar: File?,
        onLog: (String) -> Unit
    ): BuildResult = withContext(Dispatchers.IO) {
        val log = StringBuilder()
        val emit: (String) -> Unit = { line ->
            log.append(line).append("\n")
            onLog(line)
        }

        val errors = mutableListOf<BuildError>()

        try {
            val bin = ToolchainManager.binDir(context)
            val aapt2 = File(bin, "aapt2")
            val ecj = File(bin, "ecj.jar")
            val d8 = File(bin, "d8.jar")
            val apksigner = File(bin, "apksigner.jar")

            if (!aapt2.exists() || !ecj.exists() || !d8.exists()) {
                emit("[error] toolchain incomplete - run toolchain setup first")
                return@withContext BuildResult(false, null, emptyList(), log.toString())
            }

            val manifest = File(projectDir, "app/src/main/AndroidManifest.xml")
            val resDir = File(projectDir, "app/src/main/res")
            val srcDir = File(projectDir, "app/src/main/java")
            val outDir = File(projectDir, "build")
            outDir.mkdirs()

            if (!manifest.exists()) {
                emit("[error] AndroidManifest.xml missing at " + manifest.path)
                return@withContext BuildResult(false, null, emptyList(), log.toString())
            }

            // ---- Step 1: aapt2 compile resources ----
            emit("[1/5] aapt2 compile")
            val compiledRes = File(outDir, "res.zip")
            if (resDir.exists()) {
                val r = runProc(
                    listOf(aapt2.absolutePath, "compile", "--dir",
                        resDir.absolutePath, "-o", compiledRes.absolutePath),
                    projectDir, emit
                )
                if (r != 0) {
                    emit("[error] aapt2 compile failed")
                    return@withContext BuildResult(false, null, errors, log.toString())
                }
            }

            // ---- Step 2: aapt2 link ----
            emit("[2/5] aapt2 link")
            val baseApk = File(outDir, "base.apk")
            val linkCmd = mutableListOf(
                aapt2.absolutePath, "link",
                "-o", baseApk.absolutePath,
                "--manifest", manifest.absolutePath
            )
            androidJar?.let {
                linkCmd += "-I"
                linkCmd += it.absolutePath
            }
            if (compiledRes.exists()) linkCmd += compiledRes.absolutePath
            val r2 = runProc(linkCmd, projectDir, emit)
            if (r2 != 0 || !baseApk.exists()) {
                emit("[error] aapt2 link failed")
                return@withContext BuildResult(false, null, errors, log.toString())
            }

            // ---- Step 3: ecj compile Java ----
            emit("[3/5] ecj compile")
            val classesDir = File(outDir, "classes")
            classesDir.mkdirs()
            val sources = srcDir.walkTopDown()
                .filter { it.isFile && it.extension == "java" }
                .map { it.absolutePath }
                .toList()
            if (sources.isEmpty()) {
                emit("[error] no .java sources found under " + srcDir.path)
                return@withContext BuildResult(false, null, emptyList(), log.toString())
            }
            val ecjCmd = mutableListOf(
                "dalvikvm", "-cp", ecj.absolutePath,
                "org.eclipse.jdt.internal.compiler.batch.Main",
                "-1.8", "-nowarn",
                "-d", classesDir.absolutePath
            )
            androidJar?.let {
                ecjCmd += "-bootclasspath"
                ecjCmd += it.absolutePath
            }
            ecjCmd += sources
            val r3 = runProc(ecjCmd, projectDir) { line ->
                emit(line)
                parseEcjError(line)?.let { errors += it }
            }
            if (r3 != 0 || errors.isNotEmpty()) {
                emit("[error] ecj failed with " + errors.size + " error(s)")
                return@withContext BuildResult(false, null, errors, log.toString())
            }

            // ---- Step 4: d8 dexing ----
            emit("[4/5] d8 dex")
            val dexDir = File(outDir, "dex")
            dexDir.mkdirs()
            val classFiles = classesDir.walkTopDown()
                .filter { it.isFile && it.extension == "class" }
                .map { it.absolutePath }
                .toList()
            val d8Cmd = mutableListOf(
                "dalvikvm", "-cp", d8.absolutePath,
                "com.android.tools.r8.D8",
                "--output", dexDir.absolutePath
            )
            androidJar?.let {
                d8Cmd += "--lib"
                d8Cmd += it.absolutePath
            }
            d8Cmd += classFiles
            val r4 = runProc(d8Cmd, projectDir, emit)
            val dexFile = File(dexDir, "classes.dex")
            if (r4 != 0 || !dexFile.exists()) {
                emit("[error] d8 failed")
                return@withContext BuildResult(false, null, errors, log.toString())
            }

            // ---- Step 5: package + sign ----
            emit("[5/5] package + sign")
            val unsignedApk = File(outDir, "unsigned.apk")
            baseApk.copyTo(unsignedApk, overwrite = true)
            // Inject classes.dex into the apk zip
            injectDex(unsignedApk, dexFile, emit)

            val signedApk = File(outDir, "app-debug-signed.apk")
            val signOk = signApk(context, apksigner, unsignedApk, signedApk, emit)
            if (!signOk) {
                emit("[warn] signing unavailable - installing unsigned debug apk may fail")
            }

            val finalApk = if (signedApk.exists()) signedApk else unsignedApk
            emit("[done] " + finalApk.absolutePath)
            BuildResult(true, finalApk, errors, log.toString())
        } catch (t: Throwable) {
            emit("[error] " + t.message)
            BuildResult(false, null, errors, log.toString())
        }
    }

    /** Trigger the system package installer for the built APK. */
    fun installApk(context: Context, apk: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                apk
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (t: Throwable) {
            // installer trigger failed; user can install manually
        }
    }

    // ------------------------------------------------------------------

    private fun runProc(
        cmd: List<String>,
        workDir: File,
        onLine: (String) -> Unit
    ): Int {
        return try {
            val pb = ProcessBuilder(cmd)
            pb.directory(workDir)
            pb.redirectErrorStream(true)
            val process = pb.start()
            process.inputStream.bufferedReader().forEachLine { onLine(it) }
            process.waitFor()
            process.exitValue()
        } catch (t: Throwable) {
            onLine("[proc] " + t.message)
            -1
        }
    }

    private val ecjErrorRegex = Regex(
        """^.*[/\]([^/\]+\.java):(\d+):\s*(?:error|ERROR)[:\s]*(.*)$""")

    private fun parseEcjError(line: String): BuildError? {
        val m = ecjErrorRegex.find(line.trim()) ?: return null
        return BuildError(
            file = m.groupValues[1],
            line = m.groupValues[2].toIntOrNull() ?: 0,
            message = m.groupValues[3].trim()
        )
    }

    private fun injectDex(apk: File, dex: File, emit: (String) -> Unit) {
        try {
            val tmp = File(apk.parentFile, "tmp-dex.apk")
            java.util.zip.ZipFile(apk).use { zin ->
                java.util.zip.ZipOutputStream(tmp.outputStream()).use { zout ->
                    zin.entries().asSequence().forEach { entry ->
                        if (entry.name != "classes.dex") {
                            zout.putNextEntry(java.util.zip.ZipEntry(entry.name))
                            zin.getInputStream(entry).use { it.copyTo(zout) }
                            zout.closeEntry()
                        }
                    }
                    zout.putNextEntry(java.util.zip.ZipEntry("classes.dex"))
                    dex.inputStream().use { it.copyTo(zout) }
                    zout.closeEntry()
                }
            }
            tmp.renameTo(apk)
        } catch (t: Throwable) {
            emit("[warn] dex injection failed: " + t.message)
        }
    }

    private fun signApk(
        context: Context,
        apksigner: File,
        input: File,
        output: File,
        emit: (String) -> Unit
    ): Boolean {
        return try {
            // Debug keystore generation via keytool is not available on-device;
            // apksigner needs an existing keystore - use one bundled in filesDir.
            val keystore = File(ToolchainManager.binDir(context), "debug.keystore")
            if (!keystore.exists()) {
                emit("[warn] no debug.keystore - skipping sign")
                return false
            }
            val cmd = listOf(
                "dalvikvm", "-cp", apksigner.absolutePath,
                "com.android.apksigner.ApkSignerTool", "sign",
                "--ks", keystore.absolutePath,
                "--ks-pass", "pass:android",
                "--out", output.absolutePath,
                input.absolutePath
            )
            runProc(cmd, input.parentFile, emit) == 0 && output.exists()
        } catch (t: Throwable) {
            emit("[warn] sign failed: " + t.message)
            false
        }
    }
}
