package com.vibe.forge.agent

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Editable SOUL.md, same pattern as SkillLoader: seed once from assets,
 * then prefer the user-editable copy under filesDir.
 */
object AgentSoulStore {
    private const val REL_PATH = "agent/SOUL.md"

    suspend fun seedIfMissing(context: Context) = withContext(Dispatchers.IO) {
        val target = File(context.filesDir, REL_PATH)
        if (target.exists()) return@withContext
        try {
            target.parentFile?.mkdirs()
            context.assets.open(REL_PATH).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        } catch (t: Throwable) { /* no bundled SOUL.md - fine, starts blank */ }
    }

    suspend fun read(context: Context): String = withContext(Dispatchers.IO) {
        val target = File(context.filesDir, REL_PATH)
        try {
            if (target.exists()) target.readText()
            else context.assets.open(REL_PATH).bufferedReader().readText()
        } catch (t: Throwable) { "" }
    }

    suspend fun write(context: Context, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val target = File(context.filesDir, REL_PATH)
            target.parentFile?.mkdirs()
            target.writeText(content)
            true
        } catch (t: Throwable) { false }
    }
}
