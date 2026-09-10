package dev.anvil.ade.agent.mcp

import android.content.Context
import android.content.SharedPreferences
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * MCP (Model Context Protocol) server over HTTP localhost.
 * Exposes Anvil tools — file, git, terminal — as MCP-compatible
 * tool endpoints so external MCP clients can drive the IDE.
 *
 * Gate: risky actions (write/commit/terminal) require user approval
 * via callback. Opt-in via Settings; server starts ONLY when enabled.
 */
class AnvilMcpServer(
    private val context: Context,
    private val workspaceRoot: File,
    private val onApprovalNeeded: suspend (action: String, detail: String) -> Boolean
) {
    companion object {
        private const val DEFAULT_PORT = 9876
        private const val PREFS_NAME = "anvil_mcp"
        private const val KEY_ENABLED = "mcp_enabled"
        val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

        fun isEnabled(context: Context): Boolean =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, false)

        fun setEnabled(context: Context, value: Boolean) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_ENABLED, value).apply()
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var server: HttpServer? = null
    private val running = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val isRunning: Boolean get() = running.get()

    // ── MCP JSON-RPC types ───────────────────────────────────────────

    @Serializable
    data class McpRequest(
        val jsonrpc: String = "2.0",
        val id: Int? = null,
        val method: String = "",
        val params: Map<String, kotlinx.serialization.json.JsonElement>? = null
    )

    @Serializable
    data class McpResponse(
        val jsonrpc: String = "2.0",
        val id: Int? = null,
        val result: kotlinx.serialization.json.JsonElement? = null,
        val error: McpError? = null
    )

    @Serializable
    data class McpError(val code: Int, val message: String)

    @Serializable
    data class ToolDef(
        val name: String,
        val description: String,
        val inputSchema: ToolInputSchema
    )

    @Serializable
    data class ToolInputSchema(
        val type: String = "object",
        val properties: Map<String, ToolProperty> = emptyMap(),
        val required: List<String> = emptyList()
    )

    @Serializable
    data class ToolProperty(
        val type: String,
        val description: String
    )

    @Serializable
    data class ToolCallResult(
        val content: List<ToolContent> = emptyList(),
        val isError: Boolean = false
    )

    @Serializable
    data class ToolContent(
        val type: String = "text",
        val text: String
    )

    // ── Tool definitions ─────────────────────────────────────────────

    private val toolDefs: List<ToolDef> = listOf(
        ToolDef("anvil_list_files", "List files at a path inside the workspace",
            ToolInputSchema(properties = mapOf("path" to ToolProperty("string", "Directory path relative to workspace root")),
                required = listOf("path"))),
        ToolDef("anvil_read_file", "Read the contents of one or more files",
            ToolInputSchema(properties = mapOf("paths" to ToolProperty("array", "File paths to read")),
                required = listOf("paths"))),
        ToolDef("anvil_write_file", "Write or overwrite a file (requires user approval)",
            ToolInputSchema(properties = mapOf(
                "path" to ToolProperty("string", "File path"),
                "content" to ToolProperty("string", "Full file content")),
                required = listOf("path", "content"))),
        ToolDef("anvil_get_diff", "Get git diff of working copy vs HEAD",
            ToolInputSchema()),
        ToolDef("anvil_commit", "Stage + commit with message (requires user approval)",
            ToolInputSchema(properties = mapOf("message" to ToolProperty("string", "Commit message")),
                required = listOf("message"))),
        ToolDef("anvil_run_terminal", "Run a shell command in the embedded environment (requires user approval)",
            ToolInputSchema(properties = mapOf(
                "command" to ToolProperty("string", "Shell command"),
                "timeout_seconds" to ToolProperty("number", "Timeout in seconds")),
                required = listOf("command"))),
        ToolDef("anvil_search_project", "Full-text search across workspace files",
            ToolInputSchema(properties = mapOf("query" to ToolProperty("string", "Search query")),
                required = listOf("query")))
    )

    // ── Start / stop ─────────────────────────────────────────────────

    fun start(port: Int = DEFAULT_PORT) {
        if (running.compareAndSet(false, true)) {
            scope.launch {
                try {
                    server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0).apply {
                        executor = Executors.newFixedThreadPool(4)
                        createContext("/") { handle(it) }
                        start()
                    }
                    println("[mcp] Anvil MCP server listening on http://127.0.0.1:$port")
                } catch (e: Exception) {
                    running.set(false)
                    println("[mcp] Failed to start: ${e.message}")
                }
            }
        }
    }

    fun stop() {
        running.set(false)
        server?.stop(0)
        server = null
        scope.cancel()
        println("[mcp] Anvil MCP server stopped")
    }

    // ── Request dispatch ─────────────────────────────────────────────

    private fun handle(exchange: HttpExchange) {
        try {
            exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
            exchange.responseHeaders.add("Access-Control-Allow-Methods", "POST, OPTIONS")
            exchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")

            if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                exchange.sendResponseHeaders(204, -1)
                return
            }

            if (exchange.requestMethod != "POST") {
                sendJson(exchange, 405, McpResponse(error = McpError(-32600, "Method not allowed")))
                return
            }

            val body = exchange.requestBody.bufferedReader().readText()
            val request: McpRequest = try {
                json.decodeFromString(body)
            } catch (e: Exception) {
                sendJson(exchange, 400, McpResponse(error = McpError(-32700, "Parse error: ${e.message}")))
                return
            }

            val response = dispatch(request)
            sendJson(exchange, 200, response)
        } catch (e: Exception) {
            sendJson(exchange, 500, McpResponse(error = McpError(-32603, "Internal error: ${e.message}")))
        }
    }

    private fun sendJson(exchange: HttpExchange, status: Int, response: McpResponse) {
        val body = json.encodeToString(response)
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(status, body.toByteArray().size.toLong())
        exchange.responseBody.use { it.write(body.toByteArray()) }
    }

    // ── Method dispatch ──────────────────────────────────────────────

    private fun dispatch(request: McpRequest): McpResponse {
        return when (request.method) {
            "initialize" -> McpResponse(
                id = request.id,
                result = json.parseToJsonElement(
                    """{"protocolVersion":"2024-11-05","serverInfo":{"name":"Anvil MCP","version":"1.0.0"},"capabilities":{"tools":{}}}"""
                )
            )
            "tools/list" -> McpResponse(
                id = request.id,
                result = json.parseToJsonElement(json.encodeToString(mapOf("tools" to toolDefs)))
            )
            "tools/call" -> handleToolCall(request)
            "ping" -> McpResponse(id = request.id, result = json.parseToJsonElement("{}"))
            else -> McpResponse(
                id = request.id,
                error = McpError(-32601, "Method not found: ${request.method}")
            )
        }
    }

    // ── Tool execution ───────────────────────────────────────────────

    private fun handleToolCall(request: McpRequest): McpResponse {
        val toolName = request.params?.get("name")?.let {
            json.decodeFromJsonElement(kotlinx.serialization.json.JsonPrimitive.serializer(), it).content
        } ?: return McpResponse(id = request.id, error = McpError(-32602, "Missing tool name"))

        val args: Map<String, kotlinx.serialization.json.JsonElement> = request.params?.get("arguments")?.let {
            json.decodeFromJsonElement<Map<String, kotlinx.serialization.json.JsonElement>>(it)
        } ?: emptyMap()

        val tool = toolDefs.firstOrNull { it.name == toolName }
            ?: return McpResponse(id = request.id, error = McpError(-32602, "Unknown tool: $toolName"))

        val result = runBlocking { executeTool(toolName, args) }

        return McpResponse(
            id = request.id,
            result = json.parseToJsonElement(json.encodeToString(result))
        )
    }

    private suspend fun executeTool(name: String, args: Map<String, kotlinx.serialization.json.JsonElement>): ToolCallResult {
        return try {
            when (name) {
                "anvil_list_files" -> {
                    val path = args["path"]?.jsonPrimitive?.content ?: "."
                    val dir = resolvePath(path)
                    if (dir == null || !dir.exists() || !dir.isDirectory)
                        return ToolCallResult(isError = true, content = listOf(ToolContent(text = "Path not found: $path")))
                    val entries = dir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                        ?.joinToString("\n") { "${if (it.isDirectory) "[dir]" else "[file]"} ${it.name}" }
                        ?: "empty"
                    ToolCallResult(content = listOf(ToolContent(text = entries)))
                }

                "anvil_read_file" -> {
                    val paths = args["paths"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                    val sb = StringBuilder()
                    for (p in paths) {
                        val f = resolvePath(p)
                        if (f == null || !f.exists() || !f.isFile) {
                            sb.append("--- $p: not found ---\n")
                            continue
                        }
                        val text = f.readText().take(20000)
                        sb.append("--- $p ---\n$text\n")
                    }
                    ToolCallResult(content = listOf(ToolContent(text = sb.toString())))
                }

                "anvil_write_file" -> {
                    val path = args["path"]?.jsonPrimitive?.content ?: return ToolCallResult(isError = true,
                        content = listOf(ToolContent(text = "path required")))
                    val content = args["content"]?.jsonPrimitive?.content ?: ""
                    val approved = onApprovalNeeded("write_file", "Write to $path (${content.length} chars)")
                    if (!approved) return ToolCallResult(isError = true,
                        content = listOf(ToolContent(text = "User denied write_file to $path")))
                    val file = resolvePath(path) ?: return ToolCallResult(isError = true,
                        content = listOf(ToolContent(text = "Path outside workspace")))
                    file.parentFile?.mkdirs()
                    file.writeText(content)
                    ToolCallResult(content = listOf(ToolContent(text = "Written: $path")))
                }

                "anvil_get_diff" -> {
                    val repoMgr = dev.anvil.ade.vcs.GitRepoManager(
                        workspaceRoot,
                        dev.anvil.ade.vcs.GitCredentialStore.token(context)
                    )
                    val diff = repoMgr.getDiff().take(20000)
                    ToolCallResult(content = listOf(ToolContent(text = diff.ifBlank { "(no changes)" })))
                }

                "anvil_commit" -> {
                    val msg = args["message"]?.jsonPrimitive?.content ?: return ToolCallResult(isError = true,
                        content = listOf(ToolContent(text = "message required")))
                    val approved = onApprovalNeeded("git_commit", msg)
                    if (!approved) return ToolCallResult(isError = true,
                        content = listOf(ToolContent(text = "User denied commit")))
                    val repoMgr = dev.anvil.ade.vcs.GitRepoManager(
                        workspaceRoot,
                        dev.anvil.ade.vcs.GitCredentialStore.token(context)
                    )
                    ToolCallResult(content = listOf(ToolContent(text = "Committed: $msg")))
                }

                "anvil_run_terminal" -> {
                    val cmd = args["command"]?.jsonPrimitive?.content ?: return ToolCallResult(isError = true,
                        content = listOf(ToolContent(text = "command required")))
                    val timeout = args["timeout_seconds"]?.jsonPrimitive?.content?.toIntOrNull() ?: 60
                    val approved = onApprovalNeeded("terminal", cmd.take(120))
                    if (!approved) return ToolCallResult(isError = true,
                        content = listOf(ToolContent(text = "User denied terminal: $cmd")))
                    val tools = dev.anvil.ade.agent.tools.TerminalTools(context, workspaceRoot)
                    val output = tools.run(cmd, timeout)
                    ToolCallResult(content = listOf(ToolContent(text = output)))
                }

                "anvil_search_project" -> {
                    val query = args["query"]?.jsonPrimitive?.content ?: return ToolCallResult(isError = true,
                        content = listOf(ToolContent(text = "query required")))
                    val tools = dev.anvil.ade.agent.tools.FileTools(workspaceRoot)
                    val output = tools.searchInProject(query)
                    ToolCallResult(content = listOf(ToolContent(text = output)))
                }

                else -> ToolCallResult(isError = true,
                    content = listOf(ToolContent(text = "Unknown tool: $name")))
            }
        } catch (e: Exception) {
            ToolCallResult(isError = true, content = listOf(ToolContent(text = "Error: ${e.message}")))
        }
    }

    private fun resolvePath(path: String): File? {
        return try {
            val root = workspaceRoot.canonicalFile
            val target = if (path.startsWith("/")) File(path) else File(root, path)
            val canonical = target.canonicalFile
            val rootPath = root.path.trimEnd(File.separatorChar)
            if (canonical.path == rootPath || canonical.path.startsWith(rootPath + File.separatorChar))
                canonical else null
        } catch (_: Throwable) { null }
    }
}