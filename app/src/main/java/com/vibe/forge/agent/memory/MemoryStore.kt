package com.vibe.forge.agent.memory

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Per-project persistent memory.
 *
 * Stored inside the project itself:
 *   <project_root>/.vibeforge/memory.md     -> loaded into system prompt
 *   <project_root>/.vibeforge/history.jsonl -> append-only audit log,
 *                                              queried via search_history
 *
 * Rules from the blueprint:
 *  - memory.md stays concise (decisions, conventions, gotchas)
 *  - new entries require one explicit user confirmation; updates to an
 *    existing entry may be automatic
 *  - history.jsonl is never bulk-loaded into the prompt
 */
class MemoryStore(private val projectRoot: File) {

    private val dir = File(projectRoot, ".vibeforge")
    private val memoryFile = File(dir, "memory.md")
    private val historyFile = File(dir, "history.jsonl")

    private val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    // ------------------------------------------------------------------
    // memory.md
    // ------------------------------------------------------------------

    suspend fun readMemory(): String = withContext(Dispatchers.IO) {
        try {
            if (memoryFile.exists()) memoryFile.readText() else ""
        } catch (t: Throwable) {
            ""
        }
    }

    /**
     * Append or update an entry in memory.md.
     * Dedupes by exact line match - updating an existing entry replaces it
     * in place instead of duplicating.
     */
    suspend fun writeMemoryEntry(entry: String): Boolean = withContext(Dispatchers.IO) {
        try {
            dir.mkdirs()
            val trimmed = entry.trim()
            if (trimmed.isEmpty()) return@withContext false

            val existing = if (memoryFile.exists()) memoryFile.readText() else defaultHeader()
            val lines = existing.lines().toMutableList()

            // Replace an identical bullet if present; otherwise append
            val idx = lines.indexOfFirst { it.trim() == trimmed }
            if (idx >= 0) {
                lines[idx] = trimmed
            } else {
                if (lines.none { it.startsWith("## Decisions") && it.contains("Conventions") }) {
                    // ensure structure has a section to append into
                    if (lines.none { it.startsWith("## ") }) {
                        lines += ""
                        lines += "## Decisions & Conventions"
                    }
                }
                lines += trimmed
            }

            memoryFile.writeText(lines.joinToString("\n").trimEnd() + "\n")
            true
        } catch (t: Throwable) {
            false
        }
    }

    private fun defaultHeader(): String =
        "# Project Memory\n\nKept concise: decisions, conventions, gotchas.\n\n## Decisions & Conventions\n"

    // ------------------------------------------------------------------
    // history.jsonl
    // ------------------------------------------------------------------

    suspend fun appendHistory(
        instruction: String,
        filesChanged: List<String>,
        outcome: String
    ) = withContext(Dispatchers.IO) {
        try {
            dir.mkdirs()
            val record = JSONObject().apply {
                put("ts", timestamp.format(Date()))
                put("instruction", instruction.take(500))
                put("files", JSONArray(filesChanged))
                put("outcome", outcome.take(300))
            }
            historyFile.appendText(record.toString() + "\n")
        } catch (t: Throwable) {
            // history is best-effort audit, never fatal
        }
    }

    /** Search history lines containing the query (case-insensitive). */
    suspend fun searchHistory(query: String, maxResults: Int = 10): String =
        withContext(Dispatchers.IO) {
            try {
                if (!historyFile.exists()) return@withContext "no history yet"
                val q = query.lowercase()
                val hits = historyFile.readLines()
                    .filter { q in it.lowercase() }
                    .takeLast(maxResults)
                if (hits.isEmpty()) "no matches for: $query"
                else hits.joinToString("\n")
            } catch (t: Throwable) {
                "error: ${t.message}"
            }
        }
}
