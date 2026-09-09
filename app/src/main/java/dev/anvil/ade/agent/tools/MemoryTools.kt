package dev.anvil.ade.agent.tools

import dev.anvil.ade.agent.memory.MemoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Executors for the memory tools. update_memory marks whether the entry is
 * new (needs user confirmation) or an update (allowed automatically) - the
 * confirmation itself is enforced by AgentSession/UI, not here.
 */
class MemoryTools(private val store: MemoryStore) {

    suspend fun searchHistory(query: String): String = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) "error: query required"
            else store.searchHistory(query.trim())
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }

    suspend fun updateMemory(entry: String): String = withContext(Dispatchers.IO) {
        try {
            val ok = store.writeMemoryEntry(entry)
            if (ok) "memory updated" else "error: failed to write memory"
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }
}
