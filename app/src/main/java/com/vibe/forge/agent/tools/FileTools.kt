package com.vibe.forge.agent.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Native executors for file tools. Sandbox-rooted at the app workspace;
 * paths outside the sandbox are rejected.
 */
class FileTools(private val workspaceRoot: File) {

    init {
        workspaceRoot.mkdirs()
    }

    suspend fun listFiles(path: String): String = withContext(Dispatchers.IO) {
        try {
            val dir = resolve(path)
            if (dir == null || !dir.exists()) {
                return@withContext "error: path not found: $path"
            }
            if (!dir.isDirectory) {
                return@withContext "error: not a directory: $path"
            }
            val entries = dir.listFiles()
                ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                ?.joinToString("\n") {
                    (if (it.isDirectory) "[dir] " else "[file] ") + it.name
                }
            entries ?: "error: cannot list $path"
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }

    suspend fun readFiles(paths: List<String>): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        for (path in paths) {
            try {
                val file = resolve(path)
                if (file == null || !file.exists() || !file.isFile) {
                    sb.append("--- ").append(path).append(": not found ---\n")
                    continue
                }
                val text = file.readText()
                sb.append("--- ").append(path).append(" ---\n")
                sb.append(text.take(20000))
                if (text.length > 20000) sb.append("\n[truncated at 20000 chars]")
                sb.append("\n")
            } catch (t: Throwable) {
                sb.append("--- ").append(path).append(": error ${t.message} ---\n")
            }
        }
        sb.toString()
    }

    /** Resolve a path against the workspace; null if it escapes the sandbox. */
    private fun resolve(path: String): File? {
        return try {
            val root = workspaceRoot.canonicalFile
            val target = if (path.startsWith("/")) File(path) else File(root, path)
            val canonical = target.canonicalFile
            if (canonical.path.startsWith(root.path)) canonical else null
        } catch (t: Throwable) {
            null
        }
    }
}
