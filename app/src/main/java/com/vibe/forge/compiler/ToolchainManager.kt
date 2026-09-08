package com.vibe.forge.compiler

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads and prepares the MODE_A build toolchain into filesDir/bin:
 * aapt2 (ARM64, extracted from the Google Maven linux-aarch64 jar),
 * ecj.jar, d8.jar (r8), apksigner.jar. Atomic downloads, chmod on binaries.
 */
object ToolchainManager {

    private const val ECJ_URL =
        "https://repo1.maven.org/maven2/org/eclipse/jdt/ecj/3.33.0/ecj-3.33.0.jar"
    private const val R8_METADATA =
        "https://dl.google.com/dl/android/maven2/com/android/tools/r8/maven-metadata.xml"
    private const val R8_BASE =
        "https://dl.google.com/dl/android/maven2/com/android/tools/r8/"
    private const val AAPT2_METADATA =
        "https://dl.google.com/dl/android/maven2/com/android/tools/build/aapt2/maven-metadata.xml"
    private const val AAPT2_BASE =
        "https://dl.google.com/dl/android/maven2/com/android/tools/build/aapt2/"
    private const val APKSIGNER_METADATA =
        "https://dl.google.com/dl/android/maven2/com/android/tools/build/apksig/maven-metadata.xml"
    private const val APKSIGNER_BASE =
        "https://dl.google.com/dl/android/maven2/com/android/tools/build/apksig/"

    fun binDir(context: Context) = File(context.filesDir, "bin")

    fun isReady(context: Context): Boolean {
        val bin = binDir(context)
        return File(bin, "aapt2").exists() &&
                File(bin, "ecj.jar").exists() &&
                File(bin, "d8.jar").exists() &&
                File(bin, "apksigner.jar").exists()
    }

    suspend fun setup(
        context: Context,
        onProgress: (String) -> Unit
    ): List<String> = withContext(Dispatchers.IO) {
        val failed = mutableListOf<String>()
        binDir(context).mkdirs()

        suspend fun step(name: String, action: suspend () -> Boolean) {
            onProgress("fetching $name")
            try {
                if (!action()) failed += name
            } catch (t: Throwable) {
                failed += "$name (${t.message})"
            }
        }

        step("ecj.jar") {
            fetch(ECJ_URL, File(binDir(context), "ecj.jar"))
        }
        step("d8.jar") {
            val url = mavenLatest(R8_METADATA, R8_BASE) { "r8-$it.jar" }
            url != null && fetch(url, File(binDir(context), "d8.jar"))
        }
        step("apksigner.jar") {
            val url = mavenLatest(APKSIGNER_METADATA, APKSIGNER_BASE) { "apksig-$it.jar" }
            url != null && fetch(url, File(binDir(context), "apksigner.jar"))
        }
        step("aapt2") {
            downloadAapt2(context)
        }

        onProgress(if (failed.isEmpty()) "toolchain ready" else "failed: " + failed.joinToString())
        failed
    }

    private suspend fun downloadAapt2(context: Context): Boolean {
        val target = File(binDir(context), "aapt2")
        if (target.exists()) return true
        val url = mavenLatest(AAPT2_METADATA, AAPT2_BASE) { "aapt2-$it-linux-aarch64.jar" }
            ?: return false
        val tmpJar = File(context.cacheDir, "aapt2.jar.part")
        if (!fetch(url, tmpJar, keepName = true)) return false
        return try {
            java.util.zip.ZipFile(tmpJar).use { zip ->
                val entry = zip.entries().asSequence().firstOrNull { it.name == "aapt2" }
                    ?: return false
                zip.getInputStream(entry).use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
            }
            target.setExecutable(true, false)
            tmpJar.delete()
            true
        } catch (t: Throwable) {
            target.delete()
            false
        }
    }

    private fun mavenLatest(
        metadataUrl: String, baseUrl: String, artifact: (String) -> String
    ): String? {
        return try {
            val xml = httpGet(metadataUrl) ?: return null
            val release = Regex("<release>([^<]+)</release>").find(xml)?.groupValues?.get(1)
                ?: Regex("<latest>([^<]+)</latest>").find(xml)?.groupValues?.get(1)
                ?: return null
            baseUrl.trimEnd('/') + "/" + release + "/" + artifact(release)
        } catch (t: Throwable) {
            null
        }
    }

    private fun httpGet(url: String): String? {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.instanceFollowRedirects = true
            if (conn.responseCode !in 200..299) {
                conn.disconnect(); return null
            }
            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            body
        } catch (t: Throwable) {
            null
        }
    }

    private fun fetch(url: String, target: File, keepName: Boolean = false): Boolean {
        if (target.exists() && target.length() > 0 && !keepName) return true
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 60000
            conn.instanceFollowRedirects = true
            if (conn.responseCode !in 200..299) {
                conn.disconnect(); return false
            }
            val tmp = if (keepName) target
            else File(target.parentFile, target.name + ".part")
            conn.inputStream.use { input ->
                tmp.outputStream().use { output -> input.copyTo(output) }
            }
            conn.disconnect()
            if (!keepName) {
                if (target.exists()) target.delete()
                if (!tmp.renameTo(target)) return false
            }
            true
        } catch (t: Throwable) {
            if (!keepName) File(target.parentFile, target.name + ".part").delete()
            target.delete()
            false
        }
    }
}
