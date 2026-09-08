package com.vibe.forge.vcs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

/**
 * JGit-backed repo manager for MODE_B. Handles clone, branch checkout,
 * diff, commit and push to the working branch (never main/master).
 * Token comes from the credential store and is never logged.
 */
class GitRepoManager(
    private val repoDir: File,
    private val token: String
) {

    data class RepoStatus(
        val isRepo: Boolean,
        val branch: String,
        val remote: String,
        val dirty: Int
    )

    private fun credentials() =
        UsernamePasswordCredentialsProvider("token", token)

    suspend fun status(): RepoStatus = withContext(Dispatchers.IO) {
        try {
            Git.open(repoDir).use { git ->
                val branch = git.repository.branch ?: ""
                val remote = git.repository.config.getString("remote", "origin", "url") ?: ""
                val dirty = git.status().call().uncommittedChanges.size
                RepoStatus(true, branch, remote, dirty)
            }
        } catch (t: Throwable) {
            RepoStatus(false, "", "", 0)
        }
    }

    /** Clone the repo into repoDir if not already a repository. */
    suspend fun cloneIfMissing(remoteUrl: String, branch: String): String =
        withContext(Dispatchers.IO) {
            try {
                if (File(repoDir, ".git").exists()) {
                    return@withContext "repo already present"
                }
                repoDir.mkdirs()
                Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(repoDir)
                    .setBranch(branch)
                    .setCredentialsProvider(credentials())
                    .call()
                    .use { }
                "cloned: $remoteUrl ($branch)"
            } catch (t: Throwable) {
                "error: ${t.message}"
            }
        }

    /** Create (or switch to) the working branch. */
    suspend fun ensureWorkBranch(branch: String): String = withContext(Dispatchers.IO) {
        try {
            require(branch != "main" && branch != "master") {
                "refusing to work directly on $branch"
            }
            Git.open(repoDir).use { git ->
                val exists = git.repository.findRef(branch) != null
                if (exists) {
                    git.checkout().setName(branch).call()
                    "switched to $branch"
                } else {
                    git.checkout().setCreateBranch(true).setName(branch).call()
                    "created and switched to $branch"
                }
            }
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }

    /** Full diff of working copy vs HEAD. */
    suspend fun getDiff(): String = withContext(Dispatchers.IO) {
        try {
            Git.open(repoDir).use { git ->
                val out = java.io.ByteArrayOutputStream()
                git.diff()
                    .setOutputStream(out)
                    .call()
                val text = out.toString("UTF-8")
                if (text.isBlank()) "no changes"
                else text.take(30000)
            }
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }

    /** Stage all, commit and push to the working branch. */
    suspend fun commitAndPush(branch: String, message: String): String =
        withContext(Dispatchers.IO) {
            try {
                require(branch != "main" && branch != "master") {
                    "push to $branch is forbidden - use a working branch"
                }
                Git.open(repoDir).use { git ->
                    git.add().addFilepattern(".").call()
                    val status = git.status().call()
                    if (status.uncommittedChanges.isEmpty()) {
                        return@withContext "nothing to commit"
                    }
                    git.commit().setMessage(message).call()
                    git.push()
                        .setRemote("origin")
                        .setCredentialsProvider(credentials())
                        .call()
                    "pushed to $branch: $message"
                }
            } catch (t: Throwable) {
                "error: ${t.message}"
            }
        }

    /** Discard local changes to a file (Undo). */
    suspend fun checkoutFile(path: String): String = withContext(Dispatchers.IO) {
        try {
            Git.open(repoDir).use { git ->
                git.checkout().addPath(path).call()
                "reverted: $path"
            }
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }
}
