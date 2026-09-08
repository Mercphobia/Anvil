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

    val phase2Tools = listOf(listFiles, readFile)

    fun byName(name: String): ToolDefinition? = phase2Tools.firstOrNull { it.name == name }
}
