package dev.anvil.ade.agent

import android.os.FileObserver
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Event-driven automation engine. Watches the workspace for
 * file-system events (create, modify, delete) and fires user-defined
 * hooks loaded from .anvil/hooks/*.json.
 *
 * Supported triggers: on_save, on_commit, on_file_create, on_file_delete.
 * Actions: run_command, send_mcp_request, notify.
 *
 * Gate: risky actions (run_command, send_mcp_request) require
 * user approval via callback unless the hook sets "auto_approve": true.
 */
class HookEngine(
    private val workspaceRoot: File,
    private val onApprovalNeeded: suspend (hookName: String, action: String, detail: String) -> Boolean
) {
    companion object {
        private const val HOOKS_DIR = ".anvil/hooks"
    }

    // ── Hook model ───────────────────────────────────────────────────

    @Serializable
    data class HookConfig(
        val name: String,
        val description: String = "",
        val trigger: String,          // on_save | on_commit | on_file_create | on_file_delete
        val file_pattern: String = "*", // glob (simple: *.kt, *.*, or exact filename)
        val actions: List<HookAction> = emptyList(),
        val auto_approve: Boolean = false,
        val enabled: Boolean = true
    )

    @Serializable
    data class HookAction(
        val type: String,             // run_command | send_mcp_request | notify
        val command: String = "",
        val args: List<String> = emptyList(),
        val endpoint: String = ""     // for send_mcp_request
    )

    // ── State ────────────────────────────────────────────────────────

    private val hooks = ConcurrentHashMap<String, HookConfig>()
    private var fileObserver: FileObserver? = null
    private val running = AtomicBoolean(false)
    private var debounceJob: Job? = null
    private var gitPollJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastCommitHash: String? = null
    private var enabled = AtomicBoolean(false)

    val isEnabled: Boolean get() = enabled.get()
    val hookCount: Int get() = hooks.size

    // ── Lifecycle ────────────────────────────────────────────────────

    fun enable() {
        if (!enabled.compareAndSet(false, true)) return
        loadHooks()
        startFileObserver()
        startGitPolling()
    }

    fun disable() {
        enabled.set(false)
        fileObserver?.stopWatching()
        fileObserver = null
        gitPollJob?.cancel()
        debounceJob?.cancel()
        hooks.clear()
    }

    fun reload() {
        disable()
        enable()
    }

    fun setEnabled(value: Boolean) {
        if (value) enable() else disable()
    }

    // ── Load hooks from .anvil/hooks/*.json ──────────────────────────

    private fun loadHooks() {
        hooks.clear()
        val dir = File(workspaceRoot, HOOKS_DIR)
        if (!dir.exists() || !dir.isDirectory) return

        val json = Json { ignoreUnknownKeys = true }
        dir.listFiles { f -> f.extension == "json" }?.forEach { file ->
            try {
                val config = json.decodeFromString<HookConfig>(file.readText())
                if (config.enabled) {
                    hooks[config.name] = config
                }
            } catch (e: Exception) {
                println("[hooks] Failed to parse ${file.name}: ${e.message}")
            }
        }
    }

    // ── FileObserver ─────────────────────────────────────────────────

    private fun startFileObserver() {
        val rootPath = workspaceRoot.absolutePath
        val observer = object : FileObserver(rootPath,
            FileObserver.CREATE or FileObserver.MODIFY or FileObserver.DELETE or
            FileObserver.MOVED_FROM or FileObserver.MOVED_TO) {

            override fun onEvent(event: Int, path: String?) {
                if (path == null) return
                // Skip hidden dirs and hook config dir
                if (path.startsWith(".") || path.contains("/.anvil/")) return

                val trigger = when (event and FileObserver.ALL_EVENTS) {
                    FileObserver.CREATE, FileObserver.MOVED_TO -> "on_file_create"
                    FileObserver.MODIFY -> "on_save"
                    FileObserver.DELETE, FileObserver.MOVED_FROM -> "on_file_delete"
                    else -> return
                }

                scope.launch { fireHooks(trigger, path) }
            }
        }
        observer.startWatching()
        fileObserver = observer
    }

    // ── Git polling for on_commit ────────────────────────────────────

    private fun startGitPolling() {
        gitPollJob?.cancel()
        gitPollJob = scope.launch {
            while (isActive) {
                delay(3000) // poll every 3s
                try {
                    val gitDir = File(workspaceRoot, ".git")
                    if (!gitDir.exists()) continue

                    val headFile = File(gitDir, "HEAD")
                    val currentHash = resolveHead(headFile)
                    if (currentHash != null && currentHash != lastCommitHash) {
                        lastCommitHash = currentHash
                        fireHooks("on_commit", "")
                    }
                } catch (_: Exception) { /* git may not be available */ }
            }
        }
    }

    private fun resolveHead(headFile: File): String? {
        if (!headFile.exists()) return null
        val content = headFile.readText().trim()
        return if (content.startsWith("ref: ")) {
            val refPath = content.removePrefix("ref: ").trim()
            val refFile = File(workspaceRoot, ".git/$refPath")
            if (refFile.exists()) refFile.readText().trim() else null
        } else {
            content
        }
    }

    // ── Fire matching hooks ──────────────────────────────────────────

    private suspend fun fireHooks(trigger: String, filePath: String) {
        val matching = hooks.values.filter { h ->
            h.trigger == trigger && matchesPattern(h.file_pattern, filePath)
        }

        if (matching.isEmpty()) return

        // Debounce: batch rapid events (e.g. IDE auto-save) into one
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(500) // 500ms debounce window
            for (hook in matching) {
                executeHook(hook, filePath)
            }
        }
    }

    private suspend fun executeHook(hook: HookConfig, filePath: String) {
        for (action in hook.actions) {
            try {
                // Gate: risky actions need approval unless auto_approve
                val needsApproval = when (action.type) {
                    "run_command", "send_mcp_request" -> !hook.auto_approve
                    else -> false
                }

                if (needsApproval) {
                    val approved = onApprovalNeeded(
                        hook.name,
                        action.type,
                        action.command.ifBlank { action.endpoint }.take(200)
                    )
                    if (!approved) {
                        println("[hooks] User denied ${hook.name} → ${action.type}")
                        continue
                    }
                }

                when (action.type) {
                    "run_command" -> executeCommand(action.command, action.args)
                    "send_mcp_request" -> executeMcpRequest(action.endpoint, filePath)
                    "notify" -> println("[hooks] ${hook.name}: ${action.command.ifBlank { "triggered on $filePath" }}")
                }
            } catch (e: Exception) {
                println("[hooks] Error in ${hook.name}: ${e.message}")
            }
        }
    }

    // ── Action executors ─────────────────────────────────────────────

    private suspend fun executeCommand(command: String, args: List<String>): String =
        withContext(Dispatchers.IO) {
            val process = ProcessBuilder(listOf("sh", "-c", command) + args)
                .directory(workspaceRoot)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            output
        }

    private suspend fun executeMcpRequest(endpoint: String, filePath: String) {
        // Stub: MCP client call to the local server
        if (endpoint.isBlank()) return
        println("[hooks] MCP request to $endpoint for $filePath")
    }

    // ── Pattern matching ─────────────────────────────────────────────

    private fun matchesPattern(pattern: String, filePath: String): Boolean {
        if (pattern == "*" || pattern == "*.*") return true
        val fileName = filePath.substringAfterLast("/")
        return when {
            pattern.startsWith("*.") -> {
                val ext = pattern.removePrefix("*.")
                fileName.endsWith(".$ext")
            }
            pattern.contains("*") -> {
                val regex = Regex("^" + Regex.escape(pattern).replace("\\*", ".*") + "$")
                regex.matches(fileName)
            }
            else -> fileName == pattern
        }
    }
}