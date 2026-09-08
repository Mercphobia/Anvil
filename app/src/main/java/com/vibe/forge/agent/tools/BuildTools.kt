package com.vibe.forge.agent.tools

import android.content.Context
import com.vibe.forge.compiler.BuildPipelineManager
import com.vibe.forge.compiler.ToolchainManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * run_build tool executor: ensures toolchain, builds the active project,
 * and triggers the package installer on success.
 */
class BuildTools(
    private val context: Context,
    private val projectDir: File
) {

    /** Structured errors from the most recent build, read by the agent session. */
    var lastErrors: List<BuildPipelineManager.BuildError> = emptyList()
        private set

    suspend fun runBuild(onLog: (String) -> Unit): String = withContext(Dispatchers.IO) {
        try {
            if (!ToolchainManager.isReady(context)) {
                onLog("[setup] downloading toolchain...")
                val failed = ToolchainManager.setup(context, onLog)
                if (failed.isNotEmpty()) {
                    return@withContext "error: toolchain setup failed: " + failed.joinToString()
                }
            }

            // android.jar is optional but improves compile fidelity; use the
            // platform's own framework jar if present in the sandbox
            val androidJar = File(ToolchainManager.binDir(context), "android.jar")
                .takeIf { it.exists() }

            val result = BuildPipelineManager.build(context, projectDir, androidJar, onLog)
            lastErrors = result.errors
            if (result.success && result.apk != null) {
                BuildPipelineManager.installApk(context, result.apk)
                "build success: " + result.apk.absolutePath
            } else {
                val errText = if (result.errors.isEmpty()) "see log"
                else result.errors.take(5).joinToString("; ") {
                    "${it.file}:${it.line} ${it.message}"
                }
                "build failed: " + errText
            }
        } catch (t: Throwable) {
            "error: ${t.message}"
        }
    }
}
