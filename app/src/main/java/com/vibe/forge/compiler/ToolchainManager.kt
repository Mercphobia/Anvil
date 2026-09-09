package com.vibe.forge.compiler

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads aapt2 (native ARM64 binary) into filesDir/bin. ecj/d8/apksig
 * are no longer downloaded at runtime - they are Gradle dependencies of
 * Vibe Forge itself (see app/build.gradle.kts) and run in-process.
 */
object ToolchainManager {

    private const val AAPT2_METADATA =
        "https://dl.google.com/dl/android/maven2/com/android/tools/build/aapt2/maven-metadata.xml"
    private const val AAPT2_BASE =
        "https://dl.google.com/dl/android/maven2/com/android/tools/build/aapt2/"

    fun binDir(context: Context) = File(context.filesDir, "bin")

    fun isReady(context: Context): Boolean = File(binDir(context), "aapt2").exists()

    suspend fun setup(context: Context, onProgress: (String) -> Unit): List<String> = withContext(Dispatchers.IO) {
        val failed = mutableListOf<String>()
        binDir(context).mkdirs()
        onProgress("fetching aapt2")
        try {
            if (!downloadAapt2(context)) failed += "aapt2"
        } catch (t: Throwable) {
            failed += "aapt2 (${t.message})"
        }
        onProgress(if (failed.isEmpty()) "toolchain ready" else "failed: " + failed.joinToString())
        failed
    }

    private suspend fun downloadAapt2(context: Context): Boolean {
        val target = File(binDir(context), "aapt2")
        if (target.exists()) return true
        val url = mavenLatest(AAPT2_METADATA, AAPT2_BASE) { "aapt2-$it-linux-aarch64.jar" } ?: return false
        val tmpJar = File(context.cacheDir, "aapt2.jar.part")
        if (!fetch(url, tmpJar)) return false
        return try {
            java.util.zip.ZipFile(tmpJar).use { zip ->
                val entry = zip.entries().asSequence().firstOrNull { it.name == "aapt2" } ?: return false
                zip.getInputStream(entry).use { input -> target.outputStream().use { output -> input.copyTo(output) } }
            }
            target.setExecutable(true, false)
            tmpJar.delete()
            true
        } catch (t: Throwable) { target.delete(); false }
    }

    private fun mavenLatest(metadataUrl: String, baseUrl: String, artifact: (String) -> String): String? {
        return try {
            val xml = httpGet(metadataUrl) ?: return null
            val release = Regex("<release>([^<]+)</release>").find(xml)?.groupValues?.get(1)
                ?: Regex("<latest>([^<]+)</latest>").find(xml)?.groupValues?.get(1) ?: return null
            baseUrl.trimEnd('/') + "/" + release + "/" + artifact(release)
        } catch (t: Throwable) { null }
    }

    private fun httpGet(url: String): String? {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000; conn.readTimeout = 15000; conn.instanceFollowRedirects = true
            if (conn.responseCode !in 200..299) { conn.disconnect(); return null }
            val body = conn.inputStream.bufferedReader().readText(); conn.disconnect(); body
        } catch (t: Throwable) { null }
    }

    private fun fetch(url: String, target: File): Boolean {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000; conn.readTimeout = 60000; conn.instanceFollowRedirects = true
            if (conn.responseCode !in 200..299) { conn.disconnect(); return false }
            conn.inputStream.use { input -> target.outputStream().use { output -> input.copyTo(output) } }
            conn.disconnect(); true
        } catch (t: Throwable) { target.delete(); false }
    }
}
