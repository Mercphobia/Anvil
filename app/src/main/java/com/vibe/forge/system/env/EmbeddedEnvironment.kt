package com.vibe.forge.system.env

import android.content.Context
import android.system.Os
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
            // GitHub API rejects requests without a User-Agent with 403.
            conn.setRequestProperty("User-Agent", "VibeForge-Android")
            conn.setRequestProperty("Accept", "application/vnd.github+json")
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

    /**
     * True when a usable shell exists. Existence/executability is checked on
     * the given path itself — File.exists() already follows symlinks, and a
     * relative symlink (bin/sh -> dash) resolves against its own directory.
     * NOTE: do NOT use canonicalFile here: canonicalPath on Android resolves
     * against the *process* working directory, which breaks relative links
     * and can throw on dangling ones.
     */
    private fun shellUsable(f: File): Boolean {
        return try {
            f.exists() && f.isFile && f.canExecute()
        } catch (t: Throwable) {
            false
        }
    }

    /** True when [f] exists but its (symlink) target cannot be resolved. */
    private fun isBrokenLink(f: File): Boolean {
        return try {
            !f.exists() && android.system.Os.lstat(f.absolutePath) != null
        } catch (t: Throwable) {
            false
        }
    }

    fun isInstalled(context: Context): Boolean {
        return shellUsable(bashPath(context)) || shellUsable(shPath(context))
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
            // Repair passes BEFORE the isInstalled() early-return:
            //  - bash/sh physically present but not executable (partial extract)
            //  - sh is a broken symlink (target extracted later / bad target)
            //  - a previous (pre-symlink-fix) extraction never built the links
            val usr = usrDir(context)
            val bash = bashPath(context)
            val sh = shPath(context)
            val needsRepair = isBrokenLink(sh) || isBrokenLink(bash) ||
                (bash.exists() && !bash.canExecute()) ||
                (sh.exists() && !sh.canExecute()) ||
                (File(usr, "SYMLINKS.txt").exists() && !sh.exists())
            if (needsRepair) {
                onProgress("repairing environment...")
                repairPermissions(usr)
                createSymlinks(usr, onProgress)
            }
            if (isInstalled(context)) {
                return@withContext SetupReport(true, "environment ready")
            }

            // If setup previously failed, the earlier extraction may be
            // incomplete in ways repair cannot fix (missing files). Start
            // the fresh download from a clean slate.
            if (usrDir(context).exists()) {
                usrDir(context).deleteRecursively()
            }

            val zipFile = File(context.cacheDir, "bootstrap.zip.part")
            val url = resolveBootstrapUrl()
            onProgress("downloading bootstrap (~30MB)...")
            val dlError = download(url, zipFile, onProgress)
            if (dlError != null) {
                return@withContext SetupReport(false, "bootstrap download failed: $dlError")
            }

            onProgress("extracting...")
            val env = envDir(context)
            env.mkdirs()
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

            // Bootstrap zips ship symlinks as SYMLINKS.txt (target←relative/link/path)
            // because ZIP cannot store them. Recreate them — this is where bin/sh
            // (→ dash) and friends come from.
            onProgress("creating symlinks ($extracted files)...")
            val linked = createSymlinks(usr, onProgress)

            // Create home + tmp
            homeDir(context).mkdirs()
            File(usr, "tmp").mkdirs()

            // Remap prefix: create symlinks where possible, otherwise rely on env vars.
            // Many bootstrap tools read PREFIX from environment; we always set it.
            onProgress("prefix remap ($extracted files, $linked links)")

            val ok = isInstalled(context)
            SetupReport(
                ok,
                if (ok) "environment ready ($extracted files, $linked links)"
                else "extraction incomplete - no usable shell " + diagnose(context)
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
     * Java's ProcessBuilder does NOT do PATH lookup, so the last resort is an
     * absolute /system/bin/sh — a bare "sh" string fails with ENOENT (error=2).
     */
    fun shellBinary(context: Context): String {
        val bash = bashPath(context)
        if (shellUsable(bash)) return bash.absolutePath
        val sh = shPath(context)
        if (shellUsable(sh)) return sh.absolutePath
        return "/system/bin/sh"
    }

    /**
     * Human-readable state of the shell binaries, appended to the setup error
     * message so bootstrap failures can be diagnosed from the UI log alone.
     */
    private fun diagnose(context: Context): String {
        fun state(f: File): String = try {
            val lst = try { android.system.Os.lstat(f.absolutePath) != null } catch (t: Throwable) { false }
            "(exists=${f.exists()} exec=${f.canExecute()} lstat=$lst)"
        } catch (t: Throwable) {
            "(error)"
        }
        return "[bash${state(bashPath(context))} sh${state(shPath(context))}]"
    }

    /**
     * Recursively ensure everything under usr/bin, usr/libexec and usr/lib/bash
     * is executable. Extraction best-effort error handling can leave some
     * binaries without the exec bit, which makes an otherwise-complete
     * environment report "no usable shell".
     */
    private fun repairPermissions(usr: File) {
        listOf(File(usr, "bin"), File(usr, "libexec"), File(usr, "lib/bash")).forEach { dir ->
            dir.walkTopDown().forEach { f ->
                if (f.isFile) {
                    try {
                        f.setExecutable(true, false)
                        f.setReadable(true, false)
                    } catch (t: Throwable) {
                        // best-effort
                    }
                }
            }
        }
    }

    /**
     * Recreate the symlinks listed in the bootstrap's SYMLINKS.txt.
     *
     * Termux bootstrap zips cannot store real symlinks; instead the first entry
     * is SYMLINKS.txt with one mapping per line:
     *
     *     <target>←<relative/link/path>
     *
     * e.g. "dash←./bin/sh" means usr/bin/sh -> dash.
     *
     * The link path may itself be absolute (old Termux prefix
     * /data/data/com.termux/files/usr/...) — those are rewritten to our prefix.
     * If symlink creation fails (filesystem restriction), we fall back to
     * copying the resolved target file so tools still work.
     *
     * @return number of links successfully created
     */
    private fun createSymlinks(usr: File, onProgress: (String) -> Unit): Int {
        val listFile = File(usr, "SYMLINKS.txt")
        if (!listFile.exists()) return 0

        var created = 0
        listFile.readLines().forEach { raw ->
            val line = raw.trim()
            if (line.isEmpty()) return@forEach
            val sep = line.indexOf('←')
            if (sep <= 0) return@forEach
            val target = line.substring(0, sep).trim()
            var linkPath = line.substring(sep + 1).trim()
            if (linkPath.isEmpty()) return@forEach

            // Normalize: strip leading "./"; rewrite the old Termux absolute
            // prefix (and any /data/data/*/files/usr style prefix) to ours.
            linkPath = linkPath.removePrefix("./")
            val usrAbs = usr.absolutePath
            linkPath = when {
                linkPath.startsWith(usrAbs) -> linkPath.removePrefix(usrAbs).removePrefix("/")
                linkPath.startsWith(TERMUX_PREFIX) ->
                    linkPath.removePrefix(TERMUX_PREFIX).removePrefix("/")
                linkPath.startsWith("/data/data/") && linkPath.contains("/files/usr/") ->
                    linkPath.substringAfter("/files/usr/")
                else -> linkPath.removePrefix("/")
            }
            if (linkPath.isEmpty() || linkPath == "SYMLINKS.txt") return@forEach

            val linkFile = File(usr, linkPath)
            try {
                linkFile.parentFile?.mkdirs()
                if (linkFile.exists() || android.system.Os.lstat(linkFile.absolutePath) != null) {
                    linkFile.delete()
                }
            } catch (t: Throwable) {
                // lstat throws when the path doesn't exist — that's fine
            }

            try {
                Os.symlink(target, linkFile.absolutePath)
                created++
            } catch (t: Throwable) {
                // Fallback: copy the resolved target so the tool still exists.
                try {
                    val resolved = File(linkFile.parentFile, target).canonicalFile
                    if (resolved.exists() && resolved.isFile) {
                        resolved.inputStream().use { input ->
                            linkFile.outputStream().use { output -> input.copyTo(output) }
                        }
                        if (linkPath.contains("/bin/") || linkPath.contains("/libexec/")) {
                            linkFile.setExecutable(true, false)
                            linkFile.setReadable(true, false)
                        }
                        created++
                    }
                } catch (t2: Throwable) {
                    // give up on this one link
                }
            }
        }
        return created
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
            // ProcessBuilder does NOT do PATH lookup - never pass a bare "sh".
            val process = ProcessBuilder(
                "/system/bin/sh", "-c",
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

    private fun download(url: String, target: File, onProgress: (String) -> Unit): String? {
        // Returns null on success, or an error description on failure.
        repeat(2) { attempt ->
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 20000
                conn.readTimeout = 120000
                conn.instanceFollowRedirects = true
                // Some CDNs / GitHub redirect targets reject UA-less requests too.
                conn.setRequestProperty("User-Agent", "VibeForge-Android")
                val code = conn.responseCode
                if (code !in 200..299) {
                    conn.disconnect()
                    if (attempt == 1) return "HTTP $code from ${url.substringAfter("://").substringBefore("/")}"
                    return@repeat
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
                return null
            } catch (t: Throwable) {
                target.delete()
                if (attempt == 1) return "${t.javaClass.simpleName}: ${t.message}"
            }
        }
        return "download failed after retry"
    }
}
