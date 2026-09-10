package dev.anvil.ade.agent

import dev.anvil.ade.model.ProjectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Steering files (kiro-inspired): structured starting context generated ONCE
 * per project, when the project is first opened. Distinct from memory.md
 * (accumulated context): steering files rarely change after generation.
 *
 * .anvil/steering/
 *   product.md   - what this project is, for whom
 *   structure.md - architecture from folder scan + pattern detection
 *   tech.md      - detected ProjectType + dependencies from build files
 *
 * Generation needs an LLM call (sendSimple); if that fails we fall back to
 * deterministic skeleton content so the files always exist once generated.
 */
object SteeringFileGenerator {

    private const val STEERING_DIR = ".anvil/steering"
    private val STEERING_FILES = listOf("product.md", "structure.md", "tech.md")

    suspend fun isGenerated(projectRoot: File): Boolean = withContext(Dispatchers.IO) {
        val dir = File(projectRoot, STEERING_DIR)
        STEERING_FILES.all { File(dir, it).exists() }
    }

    suspend fun generate(
        projectRoot: File,
        projectType: ProjectType,
        llm: LlmClient
    ): Boolean = withContext(Dispatchers.IO) {
        val dir = File(projectRoot, STEERING_DIR)
        if (isGenerated(projectRoot)) return@withContext true

        val readme = File(projectRoot, "README.md").takeIf { it.exists() }?.readText()?.take(4000)
        val fileList = runCatching {
            projectRoot.walkTopDown()
                .filter { it.relativeTo(projectRoot).path.let { p -> !p.startsWith(".git") && !p.startsWith(".anvil") } }
                .take(200)
                .map { it.relativeTo(projectRoot).path }
                .toList()
        }.getOrDefault(emptyList())
        val configFile = readKeyConfigFile(projectRoot, projectType)

        val prompt = """
            Analyze this project and produce 3 short documents (each under 40 lines, plain markdown).
            README: ${readme ?: "(none)"}
            File structure (partial): ${fileList.joinToString("\n")}
            Config file: ${configFile ?: "(none)"}

            Reply ONLY JSON: {"product":"...","structure":"...","tech":"..."}
        """.trimIndent()

        // One-shot non-tool-use call
        val result = llm.send(prompt, emptyList(), emptyList())
        val text = result.getOrNull()?.blocks
            ?.filterIsInstance<LlmClient.ContentBlock.Text>()
            ?.joinToString("") { it.text } ?: ""

        val parsed = parseSteeringJson(text, projectType)
        dir.mkdirs()
        File(dir, "product.md").writeText(parsed.product)
        File(dir, "structure.md").writeText(parsed.structure)
        File(dir, "tech.md").writeText(parsed.tech)
        true
    }

    /** Extract the {...} JSON object from a reply that may carry markdown fences. */
    private fun parseSteeringJson(reply: String, projectType: ProjectType): Steering {
        val start = reply.indexOf('{')
        val end = reply.lastIndexOf('}')
        if (start >= 0 && end > start) {
            try {
                val obj = com.google.gson.JsonParser.parseString(reply.substring(start, end + 1)).asJsonObject
                fun g(k: String, fallback: String): String =
                    obj.get(k)?.takeIf { !it.isJsonNull }?.asString?.trim().takeUnless { it.isNullOrEmpty() } ?: fallback
                return Steering(g("product", ""), g("structure", ""), g("tech", ""))
            } catch (_: Throwable) { /* fall through to deterministic */ }
        }
        // Deterministic fallback so steering files are never empty
        return Steering(
            product = "Project overview pending - edit .anvil/steering/product.md to describe this project.",
            structure = "Structure overview pending - edit .anvil/steering/structure.md.",
            tech = "Detected project type: ${projectTypeName(projectType)}. Edit .anvil/steering/tech.md for details."
        )
    }

    private fun projectTypeName(t: ProjectType) = t.name

    private fun readKeyConfigFile(projectRoot: File, projectType: ProjectType): String? {
        val name = when (projectType) {
            ProjectType.NODE_JS -> "package.json"
            ProjectType.PYTHON -> "requirements.txt"
            ProjectType.RUST -> "Cargo.toml"
            ProjectType.GO -> "go.mod"
            ProjectType.C_CPP, ProjectType.ANDROID, ProjectType.GIT_LINKED_SYSTEM, ProjectType.GENERIC -> null
        }
        return name?.let { File(projectRoot, it).takeIf { f -> f.exists() }?.readText()?.take(4000) }
    }

    private data class Steering(val product: String, val structure: String, val tech: String)
}
