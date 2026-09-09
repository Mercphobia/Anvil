package dev.anvil.ade.agent.tools

import android.content.Context
import dev.anvil.ade.system.env.EmbeddedEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * run_terminal: executes shell commands inside the embedded bootstrap
 * environment (or system sh as fallback), sandboxed to the workspace.
 */
class TerminalTools(
    private val context: Context,
    private val workspaceRoot: File
) {

    /** Commands that could destroy the sandbox - rejected politely. */
    private val blockedPatterns = listOf(
        Regex("""\brm\s+-[rf]+\s+/(\s|$)"""),
        Regex("""\bmkfs\b"""),
        Regex("""\bdd\s+.*of=/dev/"""),
        Regex(""">\s*/dev/""")
    )

    suspend fun run(command: String, timeoutSeconds: Int = 60): String =
        withContext(Dispatchers.IO) {
            val cmd = command.trim()
            if (cmd.isEmpty()) return@withContext "error: empty command"

            if (blockedPatterns.any { it.containsMatchIn(cmd) }) {
                return@withContext "error: command blocked for sandbox safety"
            }

            // Handle cd ourselves so the working dir persists per call safely
            if (cmd == "pwd") {
                return@withContext workspaceRoot.absolutePath
            }

            try {
                // The working directory must exist before ProcessBuilder.start()
                // - a missing workDir fails with ENOENT even when the shell
                // binary itself is fine.
                workspaceRoot.mkdirs()
                val shell = EmbeddedEnvironment.shellBinary(context)
                val pb = ProcessBuilder(shell, "-c", cmd)
                pb.directory(workspaceRoot)

                val baseEnv = pb.environment()
                EmbeddedEnvironment.shellEnv(context).forEach { (k, v) ->
                    baseEnv[k] = v
                }

                pb.redirectErrorStream(true)
                val process = pb.start()

                val output = StringBuilder()
                val readerThread = Thread {
                    process.inputStream.bufferedReader().forEachLine { line ->
                        if (output.length < 8000) {
                            output.append(line).append("\n")
                        }
                    }
                }
                readerThread.start()

                val finished = process.waitFor(timeoutSeconds.toLong(), TimeUnit.SECONDS)
                if (!finished) {
                    process.destroyForcibly()
                    readerThread.join(1000)
                    return@withContext output.toString() + "[killed: timeout ${timeoutSeconds}s]"
                }
                readerThread.join(2000)

                val text = output.toString()
                val suffix = "[exit ${process.exitValue()}]"
                if (text.isBlank()) suffix else text + suffix
            } catch (t: Throwable) {
                "error: ${t.message}"
            }
        }

    /** Ensure the embedded environment exists; returns setup message. */
    suspend fun ensureEnvironment(onProgress: (String) -> Unit): String =
        withContext(Dispatchers.IO) {
            if (EmbeddedEnvironment.isInstalled(context)) {
                "environment ready"
            } else {
                val report = EmbeddedEnvironment.setup(context, onProgress)
                report.message
            }
        }
}
