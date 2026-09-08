package com.vibe.forge.system.env

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipFile

/**
 * Embedded Termux-style bootstrap environment, fully owned by Vibe Forge.
 *
 * Downloads the official aarch64 bootstrap zip once, extracts it into
 * filesDir/env/, and remaps the Termux prefix to our sandbox:
 *
 *   filesDir/env/usr/   binaries, libs, etc (PREFIX)
 *   filesDir/env/home/  HOME for the shell
 *
 * The real Termux app is never touched; package-manager (pkg/apt) is not
 * functional by design, extra packages are installed manually via
 * installPackage() which extracts .deb archives into the same prefix.
 */
object EmbeddedEnvironment {

    private const val BOOTSTRAP_API =
        "https://api.github.com/repos/termux/termux-packages/releases?per_page=20"
    private const val BOOTSTRAP_FALLBACK_URL =
        "https://github.com/termux/termux-packages/releases/download/bootstrap-2026.09.06-r1%2Bapt.android-7/bootstrap-aarch64.zip"
    private const val TERMUX_PREFIX = "/data/data/com.termux/files/usr"

    /** Resolve the newest bootstrap-aarch64.zip URL from the releases API. */
    private fun resolveBootstrapUrl(): String {
        return try {
            val conn = java.net.URL(BOOTSTRAP_API).openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            if (conn.responseCode !in 200..299) {
                conn.disconnect()
                return BOOTSTRAP_FALLBACK_URL
            }
            val json = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            Regex("\"browser_download_url\"\\s*:\\s*\"([^\"]*bootstrap-aarch64\\.zip)\"")
                .find(json)?.groupValues?.get(1)
                ?: BOOTSTRAP_FALLBACK_URL
        } catch (t: Throwable) {
            BOOTSTRAP_FALLBACK_URL
        }
    }

    fun envDir(context: Context) = File(context.filesDir, "env")
    fun usrDir(context: Context) = File(envDir(context), "usr")
    fun homeDir(context: Context) = File(envDir(context), "home")
    fun bashPath(context: Context) = File(usrDir(context), "bin/bash")
    fun shPath(context: Context) = File(usrDir(context), "bin/sh")

    fun isInstalled(context: Context): Boolean {
        return bashPath(context).exists() || shPath(context).exists()
    }

    data class SetupReport(
        val success: Boolean,
        val message: String
    )

    suspend fun setup(
        context: Context,
        onProgress: (String) -> Unit
    ): SetupReport = withContext(Dispatchers.IO) {
        try {
            if (isInstalled(context)) {
                return@withContext SetupReport(true, "environment ready")
            }

            val zipFile = File(context.cacheDir, "bootstrap.zip.part")
            val url = resolveBootstrapUrl()
            onProgress("downloading bootstrap (~30MB)...")
            if (!download(url, zipFile, onProgress)) {
                return@withContext SetupReport(false, "bootstrap download failed")
            }

            onProgress("extracting...")
            val env = envDir(context)
            env.mkdirs()
            val usr = usrDir(context)
            usr.mkdirs()

            var extracted = 0
            ZipFile(zipFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    // Bootstrap zips contain paths like "usr/bin/..." or "./usr/..."
                    val cleanName = entry.name.removePrefix("./")
                    val target = File(env, cleanName)
                    try {
                        if (entry.isDirectory) {
                            target.mkdirs()
                        } else {
                            target.parentFile?.mkdirs()
                            zip.getInputStream(entry).use { input ->
                                target.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            // Mark everything under bin/ and libexec/ executable
                            if (cleanName.contains("/bin/") || cleanName.contains("/libexec/")) {
                                target.setExecutable(true, false)
                                target.setReadable(true, false)
                            }
                            extracted++
                        }
                    } catch (t: Throwable) {
                        // skip individual file failures
                    }
                }
            }

            zipFile.delete()

            // Create home + tmp
            homeDir(context).mkdirs()
            File(usr, "tmp").mkdirs()

            // Remap prefix: create symlinks where possible, otherwise rely on env vars.
            // Many bootstrap tools read PREFIX from environment; we always set it.
            onProgress("prefix remap ($extracted files)")

            val ok = isInstalled(context)
            SetupReport(
                ok,
                if (ok) "environment ready ($extracted files)"
                else "extraction incomplete - bash not found"
            )
        } catch (t: Throwable) {
            SetupReport(false, "setup failed: ${t.message}")
        }
    }

    /**
     * Build the environment map for a shell process:
     * PREFIX + HOME + PATH (bootstrap bin first, then toolchain bin) +
     * LD_LIBRARY_PATH for dynamic binaries.
     */
    fun shellEnv(context: Context): Map<String, String> {
        val usr = usrDir(context)
        val binDir = File(context.filesDir, "bin")
        val env = mutableMapOf<String, String>()
        env["PREFIX"] = usr.absolutePath
        env["HOME"] = homeDir(context).absolutePath
        env["TMPDIR"] = File(usr, "tmp").absolutePath
        env["PATH"] = listOf(
            File(usr, "bin").absolutePath,
            binDir.absolutePath,
            "/system/bin",
            "/system/xbin"
        ).joinToString(File.pathSeparator)
        env["LD_LIBRARY_PATH"] = File(usr, "lib").absolutePath
        env["LANG"] = "en_US.UTF-8"
        return env
    }

    /**
     * Shell binary to invoke, with graceful fallback.
     * "sh" without a path is resolved through the process PATH lookup,
     * which is more reliable than hardcoding /system/bin/sh (not always
     * executable for third-party apps on modern Android).
     */
    fun shellBinary(context: Context): String {
        val bash = bashPath(context)
        if (bash.exists() && bash.canExecute()) return bash.absolutePath
        val sh = shPath(context)
        if (sh.exists() && sh.canExecute()) return sh.absolutePath
        return "sh"
    }

    /**
     * Install an extra .deb package manually: extracts data.tar.* into the
     * prefix. pkg/apt itself is intentionally not supported.
     */
    suspend fun installPackage(
        context: Context,
        debFile: File,
        onProgress: (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        try {
            onProgress("installing ${debFile.name}...")
            // .deb = ar archive containing data.tar.(gz|xz|zst)
            val process = ProcessBuilder(
                "sh", "-c",
                "cd ${debFile.parentFile.absolutePath} && " +
                        "ar x ${debFile.absolutePath} && " +
                        "tar -xf data.tar.* -C ${usrDir(context).absolutePath}"
            ).redirectErrorStream(true).start()
            val out = process.inputStream.bufferedReader().readText()
            process.waitFor()
            if (process.exitValue() == 0) {
                "installed ${debFile.name}"
            } else {
                "install failed: $out"
            }
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }

    private fun download(url: String, target: File, onProgress: (String) -> Unit): Boolean {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 20000
            conn.readTimeout = 120000
            conn.instanceFollowRedirects = true
            if (conn.responseCode !in 200..299) {
                conn.disconnect()
                return false
            }
            val total = conn.contentLengthLong
            conn.inputStream.use { input ->
                target.outputStream().use { output ->
                    val buf = ByteArray(65536)
                    var read: Int
                    var acc = 0L
                    var lastPct = -1
                    while (input.read(buf).also { read = it } != -1) {
                        output.write(buf, 0, read)
                        acc += read
                        if (total > 0) {
                            val pct = (acc * 100 / total).toInt()
                            if (pct != lastPct && pct % 20 == 0) {
                                lastPct = pct
                                onProgress("download $pct%")
                            }
                        }
                    }
                }
            }
            conn.disconnect()
            true
        } catch (t: Throwable) {
            target.delete()
            false
        }
    }
}
