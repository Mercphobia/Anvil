package com.vibe.forge.agent.tools

import com.vibe.forge.system.XmlResourceInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * MODE_B editing tools. Enforces the read-before-edit rule: an edit is
 * only executed if the target file was read earlier in this session.
 */
class MockupTools(private val workspaceRoot: File) {

    /** Paths read during this session - the edit gate checks against this. */
    private val readPaths = mutableSetOf<String>()

    fun markRead(path: String) {
        readPaths += canonical(path)?.absolutePath ?: path
    }

    fun wasRead(path: String): Boolean {
        val canon = canonical(path)?.absolutePath ?: return false
        return canon in readPaths
    }

    suspend fun editLayoutXml(path: String, newContent: String): String =
        withContext(Dispatchers.IO) {
            if (!wasRead(path)) {
                return@withContext "error: read_file must be called on $path before editing (read-before-edit rule)"
            }
            if (!XmlResourceInterceptor.isWellFormed(newContent)) {
                return@withContext "error: new content is not well-formed XML - edit rejected"
            }
            writeSafe(path, newContent)
        }

    suspend fun editKotlinLogic(path: String, newContent: String): String =
        withContext(Dispatchers.IO) {
            if (!wasRead(path)) {
                return@withContext "error: read_file must be called on $path before editing (read-before-edit rule)"
            }
            val braceError = braceCheck(newContent)
            if (braceError != null) {
                return@withContext "error: unbalanced braces in new content ($braceError) - edit rejected"
            }
            writeSafe(path, newContent)
        }

    suspend fun previewMockup(xmlContent: String): String = withContext(Dispatchers.IO) {
        try {
            if (!XmlResourceInterceptor.isWellFormed(xmlContent)) {
                return@withContext "error: XML not well-formed - preview rejected"
            }
            // Push to the shared UI state consumed by MockupScreen
            com.vibe.forge.ui.screens.MockupState.currentXml.value = xmlContent
            com.vibe.forge.ui.screens.MockupState.lastValidation.value = "XML OK"
            "preview updated in Mockup tab"
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }

    private fun writeSafe(path: String, content: String): String {
        return try {
            val file = canonical(path)
                ?: return "error: path outside workspace"
            // Backup before overwrite (Undo support in Phase 10)
            if (file.exists()) {
                val backup = File(file.parentFile, file.name + ".vibeforge.bak")
                file.copyTo(backup, overwrite = true)
            }
            file.parentFile?.mkdirs()
            file.writeText(content)
            "edited: " + file.absolutePath
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }

    private fun canonical(path: String): File? {
        return try {
            val root = workspaceRoot.canonicalFile
            val target = if (path.startsWith("/")) File(path) else File(root, path)
            val canon = target.canonicalFile
            if (canon.path.startsWith(root.path)) canon else null
        } catch (t: Throwable) {
            null
        }
    }

    /** Naive brace balance check for Kotlin/Java sources. */
    private fun braceCheck(content: String): String? {
        var depth = 0
        var inString = false
        var inChar = false
        var escaped = false
        var inLineComment = false
        var inBlockComment = false
        var i = 0
        while (i < content.length) {
            val ch = content[i]
            val next = content.getOrNull(i + 1)
            when {
                inLineComment -> if (ch == '\n') inLineComment = false
                inBlockComment -> if (ch == '*' && next == '/') {
                    inBlockComment = false; i++
                }
                escaped -> escaped = false
                inString -> when {
                    ch == '\\' -> escaped = true
                    ch == '\"' -> inString = false
                }
                inChar -> when {
                    ch == '\\' -> escaped = true
                    ch == '\'' -> inChar = false
                }
                ch == '/' && next == '/' -> inLineComment = true
                ch == '/' && next == '*' -> inBlockComment = true
                ch == '\"' -> inString = true
                ch == '\'' -> inChar = true
                ch == '{' -> depth++
                ch == '}' -> {
                    depth--
                    if (depth < 0) return "extra closing brace at offset $i"
                }
            }
            i++
        }
        return if (depth != 0) "unclosed brace (depth $depth)" else null
    }
}
