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

    suspend fun writeFile(path: String, content: String): String = withContext(Dispatchers.IO) {
        try {
            val file = resolve(path) ?: return@withContext "error: path outside workspace"
            file.parentFile?.mkdirs()
            if (file.exists()) {
                file.copyTo(java.io.File(file.parentFile, file.name + ".vibeforge.bak"), overwrite = true)
            }
            file.writeText(content)
            "written: " + file.absolutePath
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }

    suspend fun searchInProject(query: String): String = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) return@withContext "error: query required"
            val results = StringBuilder()
            var hits = 0
            workspaceRoot.walkTopDown()
                .filter { it.isFile && it.extension in listOf("kt", "java", "xml", "md", "json", "gradle", "kts") }
                .forEach { file ->
                    if (hits >= 50) return@forEach
                    try {
                        file.readLines().forEachIndexed { index, line ->
                            if (line.contains(query, ignoreCase = true) && hits < 50) {
                                results.append(file.name).append(":")
                                    .append(index + 1).append(": ")
                                    .append(line.trim().take(120)).append("\n")
                                hits++
                            }
                        }
                    } catch (t: Throwable) { }
                }
            if (hits == 0) "no matches for: $query" else results.toString()
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
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
