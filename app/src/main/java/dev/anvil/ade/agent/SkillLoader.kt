package dev.anvil.ade.agent

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Loads domain skills (static .md instruction files) into the system prompt.
 *
 * Skill sources, in priority order:
 *  1. User-editable copies under filesDir/agent/skills/<slug>/SKILL.md
 *     (seeded from assets on first run; users can edit or add new folders
 *     and they are picked up without an app rebuild)
 *  2. Bundled defaults in assets/agent/skills/<slug>/SKILL.md
 *
 * Selection is keyword-based on the user's instruction plus the active mode,
 * keeping the prompt small.
 */
object SkillLoader {

    data class Skill(
        val slug: String,
        val name: String,
        val description: String,
        val appliesTo: String,
        val body: String
    )

    private const val SKILLS_ASSET_ROOT = "agent/skills"
    private const val FRONTMATTER_DELIM = "---"

    /** Mirror bundled skills into private storage once so users can edit them. */
    suspend fun seedUserSkills(context: Context) = withContext(Dispatchers.IO) {
        try {
            val targetRoot = File(context.filesDir, SKILLS_ASSET_ROOT)
            if (targetRoot.exists()) return@withContext
            targetRoot.mkdirs()
            val slugs = context.assets.list(SKILLS_ASSET_ROOT) ?: return@withContext
            for (slug in slugs) {
                val dir = File(targetRoot, slug)
                dir.mkdirs()
                try {
                    context.assets.open("$SKILLS_ASSET_ROOT/$slug/SKILL.md").use { input ->
                        File(dir, "SKILL.md").outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (t: Throwable) {
                    // skip missing files
                }
            }
        } catch (t: Throwable) {
            // seeding is best-effort
        }
    }

    /** Discover all available skills (user folder first, assets as fallback). */
    /** Save/overwrite a SKILL.md in the user-editable folder. */
    suspend fun save(context: Context, slug: String, content: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val dir = File(File(context.filesDir, SKILLS_ASSET_ROOT), slug)
                dir.mkdirs()
                File(dir, "SKILL.md").writeText(content)
                true
            } catch (t: Throwable) { false }
        }

    /** Create a new skill with minimal frontmatter. */
    suspend fun create(context: Context, slug: String, description: String, appliesTo: String): Boolean {
        val template = """
            ---
            name: $slug
            description: $description
            applies_to: [$appliesTo]
            ---

            ## Konteks
            (isi konteks skill di sini)

            ## Aturan/Pola yang harus diikuti
            - 
        """.trimIndent()
        return save(context, slug, template)
    }

    suspend fun discover(context: Context): List<Skill> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Skill>()
        val seen = mutableSetOf<String>()

        // 1. User-editable folder
        val userRoot = File(context.filesDir, SKILLS_ASSET_ROOT)
        userRoot.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
            val file = File(dir, "SKILL.md")
            if (file.exists()) {
                parseSkill(dir.name, file.readText())?.let {
                    result += it
                    seen += it.slug
                }
            }
        }

        // 2. Bundled assets for anything not overridden
        try {
            context.assets.list(SKILLS_ASSET_ROOT)?.forEach { slug ->
                if (slug !in seen) {
                    try {
                        val text = context.assets
                            .open("$SKILLS_ASSET_ROOT/$slug/SKILL.md")
                            .bufferedReader().readText()
                        parseSkill(slug, text)?.let { result += it }
                    } catch (t: Throwable) {
                        // skip
                    }
                }
            }
        } catch (t: Throwable) {
            // no bundled skills
        }

        result
    }

    /**
     * Select skills relevant to the active mode and the user's instruction.
     * Mode match is required; keyword hits boost relevance.
     */
    fun select(
        skills: List<Skill>,
        projectType: dev.anvil.ade.model.ProjectType,
        instruction: String
    ): List<Skill> {
        // Type tag matching with legacy compatibility: existing skills carry
        // MODE_A/MODE_B tags; ANDROID maps to MODE_A, GIT_LINKED_SYSTEM to
        // MODE_B, other types match their own enum name (PYTHON, NODE_JS, ...).
        val typeTags = when (projectType) {
            dev.anvil.ade.model.ProjectType.ANDROID -> listOf("MODE_A", "ANDROID")
            dev.anvil.ade.model.ProjectType.GIT_LINKED_SYSTEM -> listOf("MODE_B", "GIT_LINKED_SYSTEM", "AOSP")
            else -> listOf(projectType.name)
        }
        val modeSkills = skills.filter { s -> typeTags.any { s.appliesTo.contains(it) } }
        if (modeSkills.isEmpty()) return emptyList()

        val lower = instruction.lowercase()
        val keywords = mapOf(
            "quick settings" to listOf("aosp-systemui"),
            "qs" to listOf("aosp-systemui"),
            "tile" to listOf("aosp-systemui"),
            "systemui" to listOf("aosp-systemui"),
            "status bar" to listOf("aosp-systemui"),
            "color" to listOf("design"),
            "warna" to listOf("design"),
            "layout" to listOf("design", "xml-resource"),
            "xml" to listOf("xml-resource"),
            "preview" to listOf("xml-resource"),
            "commit" to listOf("git-commit"),
            "push" to listOf("git-commit"),
            "branch" to listOf("git-commit"),
            "app" to listOf("android-app"),
            "calculator" to listOf("android-app")
        )

        val boosted = mutableSetOf<String>()
        keywords.forEach { (keyword, tags) ->
            if (keyword in lower) boosted += tags
        }

        val relevant = modeSkills.filter { skill ->
            boosted.any { tag -> skill.slug.contains(tag) }
        }

        // Always include type-appropriate base skills even without keyword hits
        val base = modeSkills.filter { skill ->
            (projectType == dev.anvil.ade.model.ProjectType.ANDROID && skill.slug == "android-app-builder") ||
                    (projectType == dev.anvil.ade.model.ProjectType.GIT_LINKED_SYSTEM && skill.slug == "aosp-systemui-editing")
        }

        return (base + relevant).distinctBy { it.slug }
    }

    /** Render selected skills as a system-prompt section. */
    fun renderPromptSection(skills: List<Skill>): String {
        if (skills.isEmpty()) return ""
        val sb = StringBuilder("\n\nDomain skills to follow:\n")
        skills.forEach { skill ->
            sb.append("\n--- skill: ").append(skill.slug).append(" ---\n")
            sb.append(skill.body.trim())
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun parseSkill(slug: String, text: String): Skill? {
        return try {
            val parts = text.split(FRONTMATTER_DELIM, limit = 3)
            if (parts.size < 3) return null
            val meta = parts[1]
            val body = parts[2]
            val name = metaLine(meta, "name") ?: slug
            val description = metaLine(meta, "description") ?: ""
            val appliesTo = metaLine(meta, "applies_to") ?: ""
            Skill(slug, name, description, appliesTo, body)
        } catch (t: Throwable) {
            null
        }
    }

    private fun metaLine(meta: String, key: String): String? {
        return meta.lineSequence()
            .firstOrNull { it.trim().startsWith("$key:") }
            ?.substringAfter(':')
            ?.trim()
            ?.removePrefix("[")?.removeSuffix("]")
    }
}
