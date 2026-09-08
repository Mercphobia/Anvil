package com.vibe.forge.agent.tools

import com.vibe.forge.vcs.GitRepoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Git tool executors for MODE_B. commit_and_push is gated by the UI
 * confirmation button - the agent only stages the request.
 */
class GitTools(private val repoManager: GitRepoManager) {

    suspend fun getDiff(): String = withContext(Dispatchers.IO) {
        try {
            repoManager.getDiff()
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
