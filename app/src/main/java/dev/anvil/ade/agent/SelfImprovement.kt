package dev.anvil.ade.agent

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Self-improvement: the agent can propose updates to its own skills.
 *
 * Safety model (mirrors the memory gate):
 *  - Every proposed skill edit becomes a pending proposal in the UI.
 *  - It is only written to filesDir/agent/skills/<slug>/SKILL.md after the
 *    user explicitly approves it.
 *  - The asset-bundled defaults are never touched; user-level copies win
 *    at load time, so improvements persist across sessions.
 */
object SelfImprovement {

    data class SkillProposal(
        val slug: String,
        val newContent: String,
        val reason: String
    )

    private const val SKILLS_DIR = "agent/skills"

    /** Write an approved proposal to the user-level skill folder. */
    suspend fun applyProposal(
        context: Context,
        proposal: SkillProposal
    ): String = withContext(Dispatchers.IO) {
        try {
            val safeSlug = proposal.slug.replace(Regex("[^a-z0-9-]"), "")
            if (safeSlug.isEmpty()) return@withContext "error: invalid skill slug"

            val dir = File(context.filesDir, "$SKILLS_DIR/$safeSlug")
            dir.mkdirs()
            val file = File(dir, "SKILL.md")

            // Preserve the previous version for undo/audit
            if (file.exists()) {
                val backup = File(dir, "SKILL.md.bak")
                file.copyTo(backup, overwrite = true)
            }

            // Ensure frontmatter exists so the loader can parse it
            val content = if (proposal.newContent.trimStart().startsWith("---")) {
                proposal.newContent
            } else {
                "---\nname: $safeSlug\ndescription: ${proposal.reason.take(80)}\napplies_to: [MODE_A, MODE_B]\n---\n\n" + proposal.newContent
            }

            file.writeText(content)
            "skill updated: $safeSlug"
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }

    /** Restore a skill from its .bak backup. */
    suspend fun revertSkill(context: Context, slug: String): String =
        withContext(Dispatchers.IO) {
            try {
                val dir = File(context.filesDir, "$SKILLS_DIR/$slug")
                val backup = File(dir, "SKILL.md.bak")
                val file = File(dir, "SKILL.md")
                if (!backup.exists()) return@withContext "error: no backup for $slug"
                backup.copyTo(file, overwrite = true)
                backup.delete()
                "skill reverted: $slug"
            } catch (t: Throwable) {
                "error: ${t.message}"
            }
        }
}
