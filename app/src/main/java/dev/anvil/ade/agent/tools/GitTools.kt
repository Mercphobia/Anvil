package dev.anvil.ade.agent.tools

import dev.anvil.ade.vcs.GitRepoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Git tool executors. commit_and_push is gated by the UI
 * confirmation button - the agent only stages the request.
 */
class GitTools(private val repoManager: GitRepoManager) {

    suspend fun getDiff(): String = withContext(Dispatchers.IO) {
        try {
            val diff = repoManager.getDiff()
            if (diff.length > 20000) diff.take(20000) +
                "\n[truncated at 20000 chars - use search_in_project to find specific parts]"
            else diff
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }

    /** Called ONLY from the UI confirmation button, never by the agent loop. */
    suspend fun commitAndPush(branch: String, message: String): String =
        withContext(Dispatchers.IO) {
            try {
                repoManager.commitAndPush(branch, message)
            } catch (t: Throwable) {
                "error: ${t.message}"
            }
        }
}
