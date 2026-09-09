package dev.anvil.ade.agent.tools

import android.content.Context
import dev.anvil.ade.model.ProjectType
import dev.anvil.ade.workspace.WorkspaceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Generic build pipeline for non-Android projects: runs the configured
 * install/build/run/test commands through the embedded shell environment
 * (TerminalTools - which is user-approval gated). Errors are parsed with the
 * universal `file:line:col: message` pattern for agent self-correction.
 */
object GenericBuildRunner {

    data class Result(
        val success: Boolean,
        val errors: List<String>,
        val log: String
    )

    private val errorRegex = Regex("""^.*?([\w./-]+\.(?:java|kt|py|js|ts|rs|go|c|cpp|cc|h|hpp)):(\d+)(?::(\d+))?:\s*(?:error|Error|ERROR|fatal)?[:\s]*(.+)$""")

    suspend fun run(
        context: Context,
        projectDir: File,
        config: WorkspaceConfig,
        onLog: (String) -> Unit
    ): Result = withContext(Dispatchers.IO) {
        val terminal = TerminalTools(context, projectDir)
        val errors = mutableListOf<String>()

        // 1. Install dependencies first (if configured)
        config.installCommand?.let { cmd ->
            onLog("[install] $cmd")
            val out = terminal.run(cmd, 300)
            onLog(out.take(2000))
            if (out.contains("error", ignoreCase = true) && out.startsWith("error")) {
                errors += "install failed: ${out.take(300)}"
            }
        }

        // 2. Build (if configured)
        config.buildCommand?.let { cmd ->
            onLog("[build] $cmd")
            val out = terminal.run(cmd, 300)
            onLog(out.take(4000))
            errors += parseErrors(out)
        }

        // 3. Run or test
        val runCmd = config.runCommand ?: config.testCommand
        val out = if (runCmd != null) {
            onLog("[run] $runCmd")
            terminal.run(runCmd, 120).also { onLog(it.take(4000)) }
        } else {
            "no run/test command configured".also { onLog(it) }
        }
        errors += parseErrors(out)

        Result(
            success = errors.isEmpty(),
            errors = errors.distinct(),
            log = out
        )
    }

    /** Universal compiler error pattern: file:line[:col]: message */
    private fun parseErrors(output: String): List<String> {
        return output.lineSequence()
            .mapNotNull { line -> errorRegex.find(line)?.let { "${it.groupValues[1]}:${it.groupValues[2]} ${it.groupValues[4].trim()}" } }
            .filter { !it.contains("warning", ignoreCase = true) }
            .toList()
    }
}
