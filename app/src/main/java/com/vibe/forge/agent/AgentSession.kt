package com.vibe.forge.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibe.forge.agent.memory.MemoryStore
import com.vibe.forge.agent.tools.FileTools
import com.vibe.forge.agent.tools.MemoryTools
import com.vibe.forge.agent.tools.ToolRegistry
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Drives the tool-use agent loop:
 * user message -> LLM -> (tool calls -> native execution -> tool results)*
 * -> final text answer. Reasoning steps stream to the UI via StateFlow.
 */
class AgentSession(
    private val config: ProviderConfig,
    workspaceRoot: File,
    private val appContext: android.content.Context? = null
) : ViewModel() {

    enum class Mode { MODE_A, MODE_B }

    data class Step(
        val kind: Kind,
        val text: String
    ) {
        enum class Kind { USER, AGENT_TEXT, TOOL_CALL, TOOL_RESULT, ERROR, INFO }
    }

    private val fileTools = FileTools(workspaceRoot)
    private val memoryStore = MemoryStore(workspaceRoot)
    private val memoryTools = MemoryTools(memoryStore)
    private val client = LlmClient(config)

    /** Pending memory entries awaiting one-time user confirmation. */
    private val _pendingMemoryEntry = MutableStateFlow<String?>(null)
    val pendingMemoryEntry: StateFlow<String?> = _pendingMemoryEntry.asStateFlow()

    private val _steps = MutableStateFlow<List<Step>>(emptyList())
    val steps: StateFlow<List<Step>> = _steps.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val history = mutableListOf<LlmClient.Message>()

    private var availableSkills: List<SkillLoader.Skill> = emptyList()
    private var skillsSeeded = false

    var mode: Mode = Mode.MODE_A

    /** Last skills loaded into the prompt - exposed for the debug panel. */
    var lastLoadedSkills: List<String> = emptyList()
        private set

    private suspend fun ensureSkillsLoaded() {
        val ctx = appContext ?: return
        if (!skillsSeeded) {
            SkillLoader.seedUserSkills(ctx)
            availableSkills = SkillLoader.discover(ctx)
            skillsSeeded = true
        }
    }

    private suspend fun systemPrompt(instruction: String): String {
        ensureSkillsLoaded()
        val selected = SkillLoader.select(availableSkills, mode, instruction)
        lastLoadedSkills = selected.map { it.slug }
        val skillsSection = SkillLoader.renderPromptSection(selected)
        val modeDesc = when (mode) {
            Mode.MODE_A -> "MODE_A (App Builder): generate simple Java single-Activity Android apps compiled on-device."
            Mode.MODE_B -> "MODE_B (AOSP Design Assist): help edit AOSP SystemUI sources with preview; builds happen off-device."
        }
        return "You are Vibe Forge, an on-device Android development agent. " +
                modeDesc + " " +
                "Use the provided tools to inspect the workspace before answering. " +
                "Be concise. Never fabricate file contents - read them first." +
                skillsSection +
                memorySection()
    }

    private suspend fun memorySection(): String {
        val memory = memoryStore.readMemory()
        return if (memory.isBlank()) ""
        else "\n\nProject memory (follow these decisions/conventions):\n" + memory.trim()
    }

    fun send(userText: String) {
        if (_busy.value || userText.isBlank()) return
        _busy.value = true
        append(Step(Step.Kind.USER, userText))
        history += LlmClient.Message.user(userText)
        currentInstruction = userText

        viewModelScope.launch {
            try {
                runLoop()
            } catch (t: Throwable) {
                append(Step(Step.Kind.ERROR, "session error: " + (t.message ?: "unknown")))
            } finally {
                _busy.value = false
            }
        }
    }

    private var currentInstruction: String = ""

    private suspend fun runLoop() {
        val maxRounds = 6
        repeat(maxRounds) { round ->
            val result = client.send(systemPrompt(currentInstruction), history, ToolRegistry.phase2Tools)
            val response = result.getOrElse { e ->
                append(Step(Step.Kind.ERROR, e.message ?: "request failed"))
                return
            }

            // Record assistant blocks into history
            history += LlmClient.Message("assistant", response.blocks)

            val toolCalls = response.blocks
                .filterIsInstance<LlmClient.ContentBlock.ToolUse>()
            val texts = response.blocks
                .filterIsInstance<LlmClient.ContentBlock.Text>()

            texts.forEach { append(Step(Step.Kind.AGENT_TEXT, it.text)) }

            if (toolCalls.isEmpty()) {
                // No more tool calls: conversation round complete
                return
            }

            // Execute tools natively, append results to history
            val resultBlocks = mutableListOf<LlmClient.ContentBlock>()
            for (toolUse in toolCalls) {
                val call = toolUse.call
                append(Step(Step.Kind.TOOL_CALL, call.name + " " + summarizeArgs(call.inputJson)))
                val output = executeTool(call.name, call.inputJson)
                append(Step(Step.Kind.TOOL_RESULT, output.take(400)))
                resultBlocks += LlmClient.ContentBlock.ToolResult(call.id, output)
            }
            history += LlmClient.Message("user", resultBlocks)
        }
        append(Step(Step.Kind.INFO, "max tool rounds reached"))
    }

    private suspend fun executeTool(name: String, input: JsonObject): String {
        return try {
            when (name) {
                "list_files" -> {
                    val path = input.get("path")?.asString ?: "."
                    fileTools.listFiles(path)
                }
                "read_file" -> {
                    val paths = input.getAsJsonArray("paths")
                        ?.map { it.asString } ?: emptyList()
                    if (paths.isEmpty()) "error: paths required"
                    else fileTools.readFiles(paths)
                }
                "search_history" -> {
                    val query = input.get("query")?.asString ?: ""
                    memoryTools.searchHistory(query)
                }
                "update_memory" -> {
                    val entry = input.get("entry")?.asString ?: ""
                    val isNew = input.get("is_new_entry")?.asBoolean ?: true
                    if (isNew) {
                        // New entries require explicit user confirmation first
                        _pendingMemoryEntry.value = entry
                        "pending: entry requires user confirmation before writing"
                    } else {
                        memoryTools.updateMemory(entry)
                    }
                }
                else -> "error: unknown tool: $name"
            }
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }

    private fun summarizeArgs(input: JsonObject): String {
        return try {
            input.entrySet().joinToString(" ") { (k, v) -> "$k=${v.toString().take(60)}" }
        } catch (t: Throwable) {
            ""
        }
    }

    private fun append(step: Step) {
        _steps.value = _steps.value + step
    }

    /** User confirmed the pending memory entry - write it now. */
    fun confirmMemoryEntry() {
        val entry = _pendingMemoryEntry.value ?: return
        _pendingMemoryEntry.value = null
        viewModelScope.launch {
            val result = memoryTools.updateMemory(entry)
            append(Step(Step.Kind.INFO, "memory saved: " + entry.take(80) + " [" + result + "]"))
        }
    }

    /** User rejected the pending memory entry. */
    fun dismissMemoryEntry() {
        val entry = _pendingMemoryEntry.value ?: return
        _pendingMemoryEntry.value = null
        append(Step(Step.Kind.INFO, "memory entry dismissed: " + entry.take(80)))
    }

    fun clear() {
        history.clear()
        _steps.value = emptyList()
    }
}
