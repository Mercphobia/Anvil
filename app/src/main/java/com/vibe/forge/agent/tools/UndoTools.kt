package com.vibe.forge.agent.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Undo support: restores a file from its .vibeforge.bak backup
 * (created automatically before every MockupTools/FileTools edit).
 */
class UndoTools(private val workspaceRoot: File) {

    suspend fun undoLastChange(path: String): String = withContext(Dispatchers.IO) {
        try {
            val root = workspaceRoot.canonicalFile
            val target = if (path.startsWith("/")) File(path) else File(root, path)
            val canonical = target.canonicalFile
            // Boundary check must include the separator - a plain startsWith
            // would also accept sibling folders like ".../project1-other".
            val rootPath = root.path.trimEnd(File.separatorChar)
            val isInside = canonical.path == rootPath ||
                    canonical.path.startsWith(rootPath + File.separatorChar)
            if (!isInside) {
                return@withContext "error: path outside workspace"
            }
            val backup = File(canonical.parentFile, canonical.name + ".vibeforge.bak")
            if (!backup.exists()) {
                return@withContext "error: no backup found for $path (nothing to undo)"
            }
            backup.copyTo(canonical, overwrite = true)
            backup.delete()
            "reverted: " + canonical.name
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }
}
