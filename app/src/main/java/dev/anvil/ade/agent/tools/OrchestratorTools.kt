package dev.anvil.ade.agent.tools

import dev.anvil.ade.agent.LlmClient
import dev.anvil.ade.agent.ProviderConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Multi-file orchestration: breaks a complex user instruction into
 * per-file sub-tasks, then executes each as an independent worker.
 *
 * Flow:
 *   1. planSubTasks() — ask the LLM to decompose the instruction into
 *      (file, action, description) triples.
 *   2. executeSubTasks() — run each sub-task concurrently with
 *      bounded parallelism.
 *   3. Track progress via StateFlow for UI binding.
 */

data class SubTask(
    val id: String,
    val file: String,
    val action: String,
    val description: String
)

data class SubTaskResult(
    val task: SubTask,
    val success: Boolean,
    val output: String = ""
)

class OrchestratorTools(
    private val config: ProviderConfig,
    private val workspaceRoot: File
) {
    private val client = LlmClient(config)

    private val _plannedTasks = MutableStateFlow<List<SubTask>>(emptyList())
    val plannedTasks: StateFlow<List<SubTask>> = _plannedTasks.asStateFlow()

    private val _taskResults = MutableStateFlow<Map<String, SubTaskResult>>(emptyMap())
    val taskResults: StateFlow<Map<String, SubTaskResult>> = _taskResults.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    /** Maximum concurrent sub-task workers. */
    var maxParallelism: Int = 4

    /**
     * Ask the LLM to decompose a user instruction into per-file sub-tasks.
     * Returns a list of SubTask objects stored in [plannedTasks].
     */
    suspend fun planSubTasks(
        instruction: String,
        systemPrompt: String
    ): List<SubTask> = withContext(Dispatchers.IO) {
        _busy.value = true
        try {
            val planPrompt = """
                Decompose the following instruction into per-file sub-tasks.
                For each file that needs to be created or modified, output one line
                in this exact format (JSON array of objects):

                [
                  {"file": "path/to/file.kt", "action": "CREATE|MODIFY|DELETE", "description": "what to do"}
                ]

                Instruction: $instruction

                Current workspace root: ${workspaceRoot.absolutePath}

                Rules:
                - Only output the JSON array, nothing else.
                - Each sub-task targets exactly ONE file.
                - Order by dependency (files that others depend on come first).
                - If the instruction is already simple (one file), output a single-entry array.
                - Max 12 sub-tasks.
            """.trimIndent()

            val messages = listOf(
                LlmClient.Message("user", listOf(LlmClient.ContentBlock.Text(planPrompt)))
            )

            val response = client.send(systemPrompt, messages, emptyList(), null)
            val text = response.getOrNull()?.blocks
                ?.filterIsInstance<LlmClient.ContentBlock.Text>()
                ?.joinToString("\n") { it.text } ?: "[]"

            val tasks = parseSubTaskJson(text)
            _plannedTasks.value = tasks
            tasks
        } finally {
            _busy.value = false
        }
    }

    /**
     * Execute all planned sub-tasks concurrently with bounded parallelism.
     *
     * @param worker Lambda that executes one sub-task. Receives the SubTask,
     *               returns a SubTaskResult.
     */
    suspend fun executeSubTasks(
        worker: suspend (SubTask) -> SubTaskResult
    ): List<SubTaskResult> = withContext(Dispatchers.IO) {
        _busy.value = true
        try {
            val tasks = _plannedTasks.value
            if (tasks.isEmpty()) {
                return@withContext emptyList()
            }

            val results = tasks.chunked(maxParallelism).flatMap { chunk ->
                chunk.map { task ->
                    async {
                        val result = worker(task)
                        _taskResults.value = _taskResults.value + (task.id to result)
                        result
                    }
                }.awaitAll()
            }

            results
        } finally {
            _busy.value = false
        }
    }

    /**
     * Execute sub-tasks using the LLM as the worker for each file.
     * Each sub-task gets its own lightweight agent call.
     */
    suspend fun executeSubTasksWithLlm(
        systemPrompt: String
    ): List<SubTaskResult> = executeSubTasks { task ->
        try {
            val taskPrompt = """
                Task: ${task.description}
                File: ${task.file}
                Action: ${task.action}

                The workspace root is: ${workspaceRoot.absolutePath}

                ${if (task.action == "CREATE" || task.action == "MODIFY")
                    "First read the file if it exists. Then produce the exact file content. " +
                    "If this is a CREATE, the file does not exist yet — create it. " +
                    "Output the full content of the file, prefixed with the file path on its own line."
                  else
                    "Confirm the file exists and describe what you would delete."
                }
            """.trimIndent()

            val messages = listOf(
                LlmClient.Message("user", listOf(LlmClient.ContentBlock.Text(taskPrompt)))
            )

            val response = client.send(systemPrompt, messages, toolDefinitions(), null)
            val text = response.getOrNull()?.blocks
                ?.filterIsInstance<LlmClient.ContentBlock.Text>()
                ?.joinToString("\n") { it.text } ?: ""

            SubTaskResult(task, success = true, output = text)
        } catch (e: Exception) {
            SubTaskResult(task, success = false, output = "Error: ${e.message}")
        }
    }

    /** Clear planned tasks and results. */
    fun reset() {
        _plannedTasks.value = emptyList()
        _taskResults.value = emptyMap()
        _busy.value = false
    }

    /** Inject pre-built tasks (bypass LLM planning). */
    fun setTasks(tasks: List<SubTask>) {
        _plannedTasks.value = tasks
    }

    // --- private helpers ---

    private fun parseSubTaskJson(raw: String): List<SubTask> {
        return try {
            // Extract JSON array from possibly noisy LLM output
            val start = raw.indexOf('[')
            val end = raw.lastIndexOf(']')
            if (start < 0 || end < start) return emptyList()

            val json = raw.substring(start, end + 1)
            val gson = com.google.gson.Gson()
            val arr = gson.fromJson(json, com.google.gson.JsonArray::class.java)

            arr.mapIndexed { i, el ->
                val obj = el.asJsonObject
                SubTask(
                    id = "subtask-${i}",
                    file = obj.get("file")?.asString ?: "unknown",
                    action = obj.get("action")?.asString ?: "MODIFY",
                    description = obj.get("description")?.asString ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun toolDefinitions(): List<ToolDefinition> = listOf(
        ToolDefinition(
            name = "list_files",
            description = "List files and folders at a path",
            properties = mapOf("path" to ToolParam("string", "Path to list")),
            required = listOf("path")
        ),
        ToolDefinition(
            name = "read_file",
            description = "Read file contents",
            properties = mapOf("paths" to ToolParam("array", "File paths", items = "string")),
            required = listOf("paths")
        ),
        ToolDefinition(
            name = "write_file",
            description = "Write or overwrite a file",
            properties = mapOf(
                "path" to ToolParam("string", "File path"),
                "content" to ToolParam("string", "Full file content")
            ),
            required = listOf("path", "content")
        )
    )
}