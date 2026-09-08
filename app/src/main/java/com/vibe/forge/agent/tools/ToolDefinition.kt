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

    val phase2Tools = listOf(listFiles, readFile, searchHistory, updateMemory)

    fun byName(name: String): ToolDefinition? = phase2Tools.firstOrNull { it.name == name }
}
