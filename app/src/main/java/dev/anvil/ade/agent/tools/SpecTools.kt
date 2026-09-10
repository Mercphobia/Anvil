package dev.anvil.ade.agent.tools

import dev.anvil.ade.agent.LlmClient
import dev.anvil.ade.agent.ProviderConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.util.UUID

/**
 * Spec-driven development tools. Generates structured specification artifacts
 * and tracks implementation progress.
 *
 * Output directory: .anvil/specs/<slug>/
 *   - requirements.md  (EARS-style: [WHEN]...[SHALL]...)
 *   - design.md        (architecture, data flow, component tree)
 *   - tasks.md         (checklist with [ ] / [x] markers)
 *
 * Flow:
 *   1. generateRequirements() → LLM produces EARS-style requirements
 *   2. generateDesign()       → LLM produces architecture/design doc
 *   3. generateTasks()        → LLM produces implementation checklist
 *   4. markTaskDone()         → Toggle a task line as completed
 */
class SpecTools(
    private val config: ProviderConfig,
    private val workspaceRoot: File
) {
    private val client = LlmClient(config)

    data class TaskItem(
        val index: Int,
        val description: String,
        val done: Boolean = false
    )

    private val _currentSlug = MutableStateFlow<String?>(null)
    val currentSlug: StateFlow<String?> = _currentSlug.asStateFlow()

    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    /** Generate or reuse a unique slug from the feature description. */
    fun slugFor(featureDescription: String): String {
        return featureDescription
            .take(50)
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "spec-${UUID.randomUUID().toString().take(8)}" }
    }

    private fun specsDir(slug: String): File =
        File(workspaceRoot, ".anvil/specs/$slug").also { it.mkdirs() }

    // -----------------------------------------------------------------
    // GENERATION
    // -----------------------------------------------------------------

    /**
     * Generate EARS-style requirements (Easy Approach to Requirements Syntax).
     * Format: [WHEN <trigger>] [IF <condition>] the <system> SHALL <response>.
     */
    suspend fun generateRequirements(
        featureDescription: String,
        systemPrompt: String,
        slug: String = slugFor(featureDescription)
    ): String = withContext(Dispatchers.IO) {
        _busy.value = true
        _currentSlug.value = slug
        try {
            val prompt = """
                You are a requirements engineer. Generate EARS-style requirements
                for the following feature.

                EARS format cheat sheet:
                - Ubiquitous:  The <system> SHALL <response>.
                - Event-driven: [WHEN <trigger>] the <system> SHALL <response>.
                - State-driven: [WHILE <state>] the <system> SHALL <response>.
                - Unwanted:     [IF <condition>] THEN the <system> SHALL <response>.
                - Optional:     [WHERE <feature is included>] the <system> SHALL <response>.
                - Complex:      [WHEN <trigger>] [IF <condition>] THEN the <system> SHALL <response>.

                Feature: $featureDescription

                Output a markdown document with:
                1. Title: # Requirements: <feature name>
                2. A short overview paragraph
                3. Numbered list of requirements (REQ-001, REQ-002, ...)
                4. Each requirement labeled with its EARS pattern type
                5. Non-functional requirements section at the end

                Output ONLY the markdown, no preamble.
            """.trimIndent()

            val messages = listOf(
                LlmClient.Message("user", listOf(LlmClient.ContentBlock.Text(prompt)))
            )

            val response = client.send(systemPrompt, messages, emptyList(), null)
            val content = response.getOrNull()?.blocks
                ?.filterIsInstance<LlmClient.ContentBlock.Text>()
                ?.joinToString("\n") { it.text } ?: ""

            val dir = specsDir(slug)
            val out = File(dir, "requirements.md")
            val stamped = addMetadata(content, "requirements", slug)
            out.writeText(stamped)
            stamped
        } finally {
            _busy.value = false
        }
    }

    /**
     * Generate architecture/design document from requirements.
     * Includes: architecture diagram (textual), component tree, data flow,
     * API surface, and key design decisions.
     */
    suspend fun generateDesign(
        featureDescription: String,
        systemPrompt: String,
        slug: String = slugFor(featureDescription)
    ): String = withContext(Dispatchers.IO) {
        _busy.value = true
        _currentSlug.value = slug
        try {
            val dir = specsDir(slug)
            val reqFile = File(dir, "requirements.md")
            val requirements = if (reqFile.exists()) reqFile.readText() else featureDescription

            val prompt = """
                You are a software architect. Generate a design document for the
                following requirements.

                Requirements:
                $requirements

                Produce a markdown design document with these sections:
                1. # Design: <feature name>
                2. ## Architecture Overview
                   - High-level pattern (MVVM, Clean, etc.) and why
                   - Textual architecture diagram (ASCII boxes)
                3. ## Component Tree
                   - Hierarchical list of every component/module/class
                   - For each: responsibility + dependencies
                4. ## Data Flow
                   - How data moves through the system
                   - Key interfaces/protocols
                5. ## Key Design Decisions
                   - Trade-offs made and rationale
                6. ## File Manifest
                   - List of files to create/modify with brief description

                Output ONLY the markdown, no preamble.
            """.trimIndent()

            val messages = listOf(
                LlmClient.Message("user", listOf(LlmClient.ContentBlock.Text(prompt)))
            )

            val response = client.send(systemPrompt, messages, emptyList(), null)
            val content = response.getOrNull()?.blocks
                ?.filterIsInstance<LlmClient.ContentBlock.Text>()
                ?.joinToString("\n") { it.text } ?: ""

            val out = File(dir, "design.md")
            val stamped = addMetadata(content, "design", slug)
            out.writeText(stamped)
            stamped
        } finally {
            _busy.value = false
        }
    }

    /**
     * Generate implementation task checklist from requirements + design.
     * Each task is a checkbox line that can be toggled via markTaskDone().
     */
    suspend fun generateTasks(
        featureDescription: String,
        systemPrompt: String,
        slug: String = slugFor(featureDescription)
    ): List<TaskItem> = withContext(Dispatchers.IO) {
        _busy.value = true
        _currentSlug.value = slug
        try {
            val dir = specsDir(slug)
            val reqFile = File(dir, "requirements.md")
            val designFile = File(dir, "design.md")
            val context = buildString {
                if (reqFile.exists()) append(reqFile.readText()).append("\n\n")
                if (designFile.exists()) append(designFile.readText()).append("\n\n")
                if (isEmpty()) append(featureDescription)
            }

            val prompt = """
                You are a project planner. From the following requirements and design,
                produce an ordered implementation task checklist.

                Context:
                $context

                Rules:
                - Each task is one concrete, testable action
                - Order by dependency (setup → core → integration → polish)
                - Tasks reference specific files from the design's File Manifest
                - Max 20 tasks
                - Output format: one task per line, starting with "- [ ] "

                Output ONLY the checklist lines, no preamble.
            """.trimIndent()

            val messages = listOf(
                LlmClient.Message("user", listOf(LlmClient.ContentBlock.Text(prompt)))
            )

            val response = client.send(systemPrompt, messages, emptyList(), null)
            val content = response.getOrNull()?.blocks
                ?.filterIsInstance<LlmClient.ContentBlock.Text>()
                ?.joinToString("\n") { it.text } ?: ""

            val items = parseTaskChecklist(content)
            _tasks.value = items

            // Write tasks.md
            val tasksMd = buildString {
                appendLine("# Tasks: $featureDescription")
                appendLine()
                appendLine("> Auto-generated ${Instant.now()}")
                appendLine()
                items.forEach { task ->
                    appendLine("- [${if (task.done) "x" else " "}] ${task.description}")
                }
            }
            File(dir, "tasks.md").writeText(addMetadata(tasksMd, "tasks", slug))

            items
        } finally {
            _busy.value = false
        }
    }

    // -----------------------------------------------------------------
    // TASK MANAGEMENT
    // -----------------------------------------------------------------

    /**
     * Toggle a task's done state by its 0-based index.
     * Updates the in-memory list AND rewrites tasks.md.
     */
    fun markTaskDone(index: Int, done: Boolean = true): TaskItem? {
        val current = _tasks.value.toMutableList()
        if (index < 0 || index >= current.size) return null

        val updated = current[index].copy(done = done)
        current[index] = updated
        _tasks.value = current

        // Sync to tasks.md
        val slug = _currentSlug.value ?: return updated
        val tasksFile = File(specsDir(slug), "tasks.md")
        if (tasksFile.exists()) {
            val newContent = buildString {
                appendLine("# Tasks")
                appendLine()
                current.forEach { task ->
                    appendLine("- [${if (task.done) "x" else " "}] ${task.description}")
                }
            }
            tasksFile.writeText(newContent)
        }

        return updated
    }

    /**
     * Mark a task done by matching its description substring.
     * Returns the updated TaskItem or null if no match.
     */
    fun markTaskDoneByDescription(substring: String, done: Boolean = true): TaskItem? {
        val idx = _tasks.value.indexOfFirst { it.description.contains(substring, ignoreCase = true) }
        if (idx < 0) return null
        return markTaskDone(idx, done)
    }

    /** Get completion stats: (done, total, percentage). */
    fun completionStats(): Triple<Int, Int, Float> {
        val all = _tasks.value
        if (all.isEmpty()) return Triple(0, 0, 0f)
        val done = all.count { it.done }
        return Triple(done, all.size, done.toFloat() / all.size)
    }

    /** Load tasks from an existing tasks.md file. */
    fun loadTasks(slug: String): List<TaskItem> {
        _currentSlug.value = slug
        val tasksFile = File(specsDir(slug), "tasks.md")
        if (!tasksFile.exists()) return emptyList()
        val items = parseTaskChecklist(tasksFile.readText())
        _tasks.value = items
        return items
    }

    /**
     * Run full spec pipeline: requirements → design → tasks.
     * Returns the slug so callers can reference the output directory.
     */
    suspend fun generateFullSpec(
        featureDescription: String,
        systemPrompt: String,
        slug: String = slugFor(featureDescription)
    ): String = withContext(Dispatchers.IO) {
        generateRequirements(featureDescription, systemPrompt, slug)
        generateDesign(featureDescription, systemPrompt, slug)
        generateTasks(featureDescription, systemPrompt, slug)
        slug
    }

    /** Clear spec cache for a slug. */
    fun clearSpec(slug: String) {
        val dir = specsDir(slug)
        if (dir.exists()) dir.deleteRecursively()
        if (_currentSlug.value == slug) {
            _currentSlug.value = null
            _tasks.value = emptyList()
        }
    }

    // --- private helpers ---

    private fun addMetadata(content: String, kind: String, slug: String): String {
        val header = "<!--\n" +
                "  kind: $kind\n" +
                "  slug: $slug\n" +
                "  generated: ${Instant.now()}\n" +
                "-->\n\n"
        return if (content.startsWith("<!--")) content else header + content
    }

    private fun parseTaskChecklist(raw: String): List<TaskItem> {
        val pattern = Regex("""^-\s*\[(\s|x)\]\s+(.+)$""", RegexOption.MULTILINE)
        return pattern.findAll(raw).mapIndexed { i, match ->
            TaskItem(
                index = i,
                description = match.groupValues[2].trim(),
                done = match.groupValues[1] == "x"
            )
        }.toList()
    }
}