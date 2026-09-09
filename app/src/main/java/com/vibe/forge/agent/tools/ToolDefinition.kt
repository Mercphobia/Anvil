package com.vibe.forge.agent.tools

/**
 * Tool definitions exposed to the LLM. Phase 2 starts with read-only
 * file tools (list_files, read_file) per the blueprint.
 */
data class ToolDefinition(
    val name: String,
    val description: String,
    val properties: Map<String, ToolParam>,
    val required: List<String>
)

data class ToolParam(
    val type: String,
    val description: String,
    val items: String? = null
)

object ToolRegistry {

    val listFiles = ToolDefinition(
        name = "list_files",
        description = "List files and folders at a path inside the active workspace",
        properties = mapOf(
            "path" to ToolParam("string", "Absolute path or workspace-relative path to list")
        ),
        required = listOf("path")
    )

    val readFile = ToolDefinition(
        name = "read_file",
        description = "Read the contents of one or more files. MUST be called before any edit.",
        properties = mapOf(
            "paths" to ToolParam("array", "File paths to read", items = "string")
        ),
        required = listOf("paths")
    )

    val searchHistory = ToolDefinition(
        name = "search_history",
        description = "Query the project's change/decision history. Only call when the user explicitly asks to see history.",
        properties = mapOf(
            "query" to ToolParam("string", "Search query")
        ),
        required = listOf("query")
    )

    val updateMemory = ToolDefinition(
        name = "update_memory",
        description = "Add or update a concise entry in the project memory (decisions, conventions, gotchas).",
        properties = mapOf(
            "entry" to ToolParam("string", "One-line memory entry"),
            "is_new_entry" to ToolParam("boolean", "True if this is a brand-new entry (requires user confirmation)")
        ),
        required = listOf("entry", "is_new_entry")
    )

    val writeFile = ToolDefinition(
        name = "write_file",
        description = "Write or overwrite a file in the local workspace (MODE_A projects)",
        properties = mapOf(
            "path" to ToolParam("string", "Workspace-relative file path"),
            "content" to ToolParam("string", "Full file content")
        ),
        required = listOf("path", "content")
    )

    val runBuild = ToolDefinition(
        name = "run_build",
        description = "Run the on-device build pipeline (aapt2 -> ecj -> d8 -> sign) and trigger the package installer",
        properties = emptyMap(),
        required = emptyList()
    )

    val editLayoutXml = ToolDefinition(
        name = "edit_layout_xml",
        description = "Edit an AOSP layout XML file. read_file MUST have been called on this file earlier in the session.",
        properties = mapOf(
            "path" to ToolParam("string", "File path"),
            "new_content" to ToolParam("string", "Full new file content")
        ),
        required = listOf("path", "new_content")
    )

    val editKotlinLogic = ToolDefinition(
        name = "edit_kotlin_logic",
        description = "Edit an AOSP Kotlin/Java logic file. read_file MUST have been called on this file earlier in the session.",
        properties = mapOf(
            "path" to ToolParam("string", "File path"),
            "new_content" to ToolParam("string", "Full new file content")
        ),
        required = listOf("path", "new_content")
    )

    val previewMockup = ToolDefinition(
        name = "preview_mockup",
        description = "Render an XML layout string in the on-device mockup preview",
        properties = mapOf(
            "xml_content" to ToolParam("string", "Raw layout XML")
        ),
        required = listOf("xml_content")
    )

    val getDiff = ToolDefinition(
        name = "get_diff",
        description = "Get the diff of all changes in the working copy vs HEAD. Read-only.",
        properties = emptyMap(),
        required = emptyList()
    )

    val searchInProject = ToolDefinition(
        name = "search_in_project",
        description = "Full-text search across the active workspace (file:line results)",
        properties = mapOf(
            "query" to ToolParam("string", "Search query")
        ),
        required = listOf("query")
    )

    val getBuildErrors = ToolDefinition(
        name = "get_build_errors",
        description = "Get structured errors from the last build run (file, line, message)",
        properties = emptyMap(),
        required = emptyList()
    )

    val undoLastChange = ToolDefinition(
        name = "undo_last_change",
        description = "Revert a file to its state before the last edit (restores from local backup)",
        properties = mapOf(
            "path" to ToolParam("string", "File path to revert")
        ),
        required = listOf("path")
    )

    val proposeSkillUpdate = ToolDefinition(
        name = "propose_skill_update",
        description = "Propose an improvement to one of the agent's own skills. The user must approve before it is applied.",
        properties = mapOf(
            "slug" to ToolParam("string", "Skill slug (e.g. android-app-builder)"),
            "new_content" to ToolParam("string", "Full new SKILL.md content"),
            "reason" to ToolParam("string", "Why this improvement helps")
        ),
        required = listOf("slug", "new_content", "reason")
    )

    val runTerminal = ToolDefinition(
        name = "run_terminal",
        description = "Run a shell command inside the embedded environment, working directory = project workspace. Unix tools available (bash, coreutils, grep, find, sed, tar).",
        properties = mapOf(
            "command" to ToolParam("string", "Shell command to execute"),
            "timeout_seconds" to ToolParam("number", "Optional timeout (default 60)")
        ),
        required = listOf("command")
    )

    val proposeSoulUpdate = ToolDefinition(
        name = "propose_soul_update",
        description = "Propose an improvement to the agent's own SOUL.md (identity/behavior). The user must approve before it is applied.",
        properties = mapOf(
            "new_content" to ToolParam("string", "Full new SOUL.md content"),
            "reason" to ToolParam("string", "Why this improvement helps")
        ),
        required = listOf("new_content", "reason")
    )

    val phase2Tools = listOf(
        listFiles, readFile, writeFile, runBuild,
        editLayoutXml, editKotlinLogic, previewMockup, getDiff,
        searchInProject, getBuildErrors, undoLastChange, runTerminal,
        searchHistory, updateMemory, proposeSkillUpdate, proposeSoulUpdate
    )

    fun byName(name: String): ToolDefinition? = phase2Tools.firstOrNull { it.name == name }
}
