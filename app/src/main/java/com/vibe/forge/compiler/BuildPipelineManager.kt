package com.vibe.forge.compiler

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.android.apksig.ApkSigner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * STANDALONE_WORKSPACE build pipeline: aapt2 (subprocess, native binary) ->
 * ecj (in-process, JVM API) -> d8 (in-process, JVM API) -> apksig (in-process) ->
 * install intent. Java-only sources, no external dependencies.
 *
 * ecj/d8/apksig run IN-PROCESS via their programmatic Java API because they
 * are regular JVM bytecode - they cannot be exec'd via dalvikvm as a
 * subprocess. They are dexed automatically by AGP as normal Gradle
 * dependencies of this app (see app/build.gradle.kts).
 */
object BuildPipelineManager {

    data class BuildError(val file: String, val line: Int, val message: String)
    data class BuildResult(val success: Boolean, val apk: File?, val errors: List<BuildError>, val log: String)

    suspend fun build(
        context: Context,
        projectDir: File,
        androidJar: File?,
        onLog: (String) -> Unit
    ): BuildResult = withContext(Dispatchers.IO) {
        val log = StringBuilder()
        val emit: (String) -> Unit = { line -> log.append(line).append("\n"); onLog(line) }
        val errors = mutableListOf<BuildError>()

        try {
            val bin = ToolchainManager.binDir(context)
            val aapt2 = File(bin, "aapt2")
            if (!aapt2.exists()) {
                emit("[error] aapt2 missing - run toolchain setup first")
                return@withContext BuildResult(false, null, emptyList(), log.toString())
            }

            val manifest = File(projectDir, "app/src/main/AndroidManifest.xml")
            val resDir = File(projectDir, "app/src/main/res")
            val srcDir = File(projectDir, "app/src/main/java")
            val outDir = File(projectDir, "build").apply { mkdirs() }

            if (!manifest.exists()) {
                emit("[error] AndroidManifest.xml missing at " + manifest.path)
                return@withContext BuildResult(false, null, emptyList(), log.toString())
            }

            // ---- Step 1: aapt2 compile (native subprocess - correct as-is) ----
            emit("[1/5] aapt2 compile")
            val compiledRes = File(outDir, "res.zip")
            if (resDir.exists()) {
                val r = runProc(listOf(aapt2.absolutePath, "compile", "--dir", resDir.absolutePath, "-o", compiledRes.absolutePath), projectDir, emit)
                if (r != 0) { emit("[error] aapt2 compile failed"); return@withContext BuildResult(false, null, errors, log.toString()) }
            }

            // ---- Step 2: aapt2 link (native subprocess - correct as-is) ----
            emit("[2/5] aapt2 link")
            val baseApk = File(outDir, "base.apk")
            val linkCmd = mutableListOf(aapt2.absolutePath, "link", "-o", baseApk.absolutePath, "--manifest", manifest.absolutePath)
            androidJar?.let { linkCmd += "-I"; linkCmd += it.absolutePath }
            if (compiledRes.exists()) linkCmd += compiledRes.absolutePath
            val r2 = runProc(linkCmd, projectDir, emit)
            if (r2 != 0 || !baseApk.exists()) { emit("[error] aapt2 link failed"); return@withContext BuildResult(false, null, errors, log.toString()) }

            // ---- Step 3: ECJ compile Java, IN-PROCESS ----
            emit("[3/5] ecj compile (in-process)")
            val classesDir = File(outDir, "classes").apply { mkdirs() }
            val sources = srcDir.walkTopDown().filter { it.isFile && it.extension == "java" }.map { it.absolutePath }.toList()
            if (sources.isEmpty()) {
                emit("[error] no .java sources found under " + srcDir.path)
                return@withContext BuildResult(false, null, emptyList(), log.toString())
            }
            val ecjArgs = mutableListOf("-1.8", "-nowarn", "-d", classesDir.absolutePath)
            androidJar?.let { ecjArgs += "-bootclasspath"; ecjArgs += it.absolutePath }
            ecjArgs += sources

            val ecjOut = StringWriter()
            val ecjErr = StringWriter()
            // Main.compile(...) returns boolean and does NOT call System.exit,
            // unlike Main.main(...) - safe to call in-process.
            val ecjOk = try {
                org.eclipse.jdt.internal.compiler.batch.Main.compile(
                    ecjArgs.toTypedArray(),
                    PrintWriter(ecjOut),
                    PrintWriter(ecjErr),
                    null
                )
            } catch (t: Throwable) {
                emit("[error] ecj threw: ${t.message}")
                false
            }
            ecjOut.toString().lineSequence().forEach { l -> if (l.isNotBlank()) emit(l) }
            ecjErr.toString().lineSequence().forEach { l ->
                if (l.isNotBlank()) { emit(l); parseEcjError(l)?.let { errors += it } }
            }
            if (!ecjOk || errors.isNotEmpty()) {
                emit("[error] ecj failed with " + errors.size + " error(s)")
                return@withContext BuildResult(false, null, errors, log.toString())
            }

            // ---- Step 4: D8 dexing, IN-PROCESS ----
            emit("[4/5] d8 dex (in-process)")
            val dexDir = File(outDir, "dex").apply { mkdirs() }
            val classFiles = classesDir.walkTopDown().filter { it.isFile && it.extension == "class" }.map { it.absolutePath }.toList()

            val d8CommandBuilder = com.android.tools.r8.D8Command.builder()
                .setOutput(dexDir.toPath(), com.android.tools.r8.OutputMode.DexIndexed)
            classFiles.forEach { d8CommandBuilder.addProgramFiles(File(it).toPath()) }
            androidJar?.let { d8CommandBuilder.addLibraryFiles(it.toPath()) }

            val d8Ok = try {
                com.android.tools.r8.D8.run(d8CommandBuilder.build())
                true
            } catch (t: Throwable) {
                emit("[error] d8 failed: ${t.message}")
                false
            }
            val dexFile = File(dexDir, "classes.dex")
            if (!d8Ok || !dexFile.exists()) {
                emit("[error] d8 produced no output")
                return@withContext BuildResult(false, null, errors, log.toString())
            }

            // ---- Step 5: package + sign, IN-PROCESS via apksig ----
            emit("[5/5] package + sign")
            val unsignedApk = File(outDir, "unsigned.apk")
            baseApk.copyTo(unsignedApk, overwrite = true)
            injectDex(unsignedApk, dexFile, emit)

            val signedApk = File(outDir, "app-debug-signed.apk")
            val signOk = signApk(context, unsignedApk, signedApk, emit)
            if (!signOk) emit("[warn] signing failed - installing unsigned apk may fail")

            val finalApk = if (signedApk.exists()) signedApk else unsignedApk
            emit("[done] " + finalApk.absolutePath)
            BuildResult(true, finalApk, errors, log.toString())
        } catch (t: Throwable) {
            emit("[error] " + t.message)
            BuildResult(false, null, errors, log.toString())
        }
    }

    fun installApk(context: Context, apk: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", apk)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (t: Throwable) { /* user can install manually */ }
    }

    // ---- aapt2 subprocess helper (unchanged - this part was already correct) ----
    private fun runProc(cmd: List<String>, workDir: File, onLine: (String) -> Unit): Int {
        return try {
            val pb = ProcessBuilder(cmd); pb.directory(workDir); pb.redirectErrorStream(true)
            val process = pb.start()
            process.inputStream.bufferedReader().forEachLine { onLine(it) }
            process.waitFor(); process.exitValue()
        } catch (t: Throwable) { onLine("[proc] " + t.message); -1 }
    }

    private val ecjErrorRegex = Regex("""^.*[/\\]([^/\\]+\.java):(\d+):\s*(?:error|ERROR)[:\s]*(.*)$""")
    private fun parseEcjError(line: String): BuildError? {
        val m = ecjErrorRegex.find(line.trim()) ?: return null
        return BuildError(m.groupValues[1], m.groupValues[2].toIntOrNull() ?: 0, m.groupValues[3].trim())
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
        } catch (t: Throwable) { emit("[warn] dex injection failed: " + t.message) }
    }

    /** Sign in-process using apksig's programmatic ApkSigner.Builder - no CLI jar needed. */
    private fun signApk(context: Context, input: File, output: File, emit: (String) -> Unit): Boolean {
        return try {
            val ksFile = DebugKeystoreManager.getOrCreate(context)
            val (privateKey, cert) = DebugKeystoreManager.loadPrivateKeyAndCert(ksFile)
            val signerConfig = ApkSigner.SignerConfig.Builder(
                "vibeforge-debug", privateKey, listOf(cert)
            ).build()

            ApkSigner.Builder(listOf(signerConfig))
                .setInputApk(input)
                .setOutputApk(output)
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .setV3SigningEnabled(true)
                .build()
                .sign()

            output.exists()
        } catch (t: Throwable) {
            emit("[warn] sign failed: " + t.message)
            false
        }
    }
}
