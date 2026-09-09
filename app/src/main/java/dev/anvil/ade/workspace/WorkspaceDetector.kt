package dev.anvil.ade.workspace

import android.content.Context
import dev.anvil.ade.model.ProjectType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.google.gson.Gson

/**
 * Per-project workspace configuration, persisted at
 * `<project>/.anvil/workspace.json`. Holds the detected/overridden project
 * type plus the commands the generic shell pipeline runs (install, build,
 * run, test). Auto-filled from detection, manually editable via Agent Config.
 */
data class WorkspaceConfig(
    val projectType: ProjectType = ProjectType.GENERIC,
    val runCommand: String? = null,
    val testCommand: String? = null,
    val installCommand: String? = null,
    val buildCommand: String? = null
) {
    companion object {
        private const val FILE_NAME = "workspace.json"
        private val gson = Gson()

        fun configDir(projectRoot: File): File = File(projectRoot, ".anvil")

        fun configPath(projectRoot: File): File = File(configDir(projectRoot), FILE_NAME)

        /** Load the config; falls back to detection results when absent. */
        suspend fun load(projectRoot: File): WorkspaceConfig = withContext(Dispatchers.IO) {
            val f = configPath(projectRoot)
            if (f.exists()) {
                try {
                    val raw = gson.fromJson(f.readText(), WorkspaceConfig::class.java)
                    if (raw != null) return@withContext raw
                } catch (t: Throwable) { /* corrupt file -> re-detect */ }
            }
            WorkspaceDetector.detect(projectRoot)
        }

        suspend fun save(projectRoot: File, config: WorkspaceConfig): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    configDir(projectRoot).mkdirs()
                    configPath(projectRoot).writeText(gson.toJson(config))
                    true
                } catch (t: Throwable) { false }
            }
    }
}

/**
 * Marker-file based project type detection - the same approach modern IDEs
 * use. First match wins, in priority order. Falls back to GENERIC so the
 * agent can still work on arbitrary folders with files + shell.
 */
object WorkspaceDetector {

    suspend fun detect(projectRoot: File): WorkspaceConfig = withContext(Dispatchers.IO) {
        defaultConfigFor(detectType(projectRoot))
    }

    /** Sensible default shell commands per project type. */
    fun defaultConfigFor(type: ProjectType): WorkspaceConfig =
        when (type) {
            ProjectType.ANDROID -> WorkspaceConfig(
                projectType = type, // native aapt2/ecj/d8 pipeline - no shell commands needed
                runCommand = null, testCommand = null, installCommand = null, buildCommand = null
            )
            ProjectType.NODE_JS -> WorkspaceConfig(
                projectType = type,
                installCommand = "npm install",
                runCommand = "npm start",
                testCommand = "npm test",
                buildCommand = "npm run build"
            )
            ProjectType.PYTHON -> WorkspaceConfig(
                projectType = type,
                installCommand = "pip install -r requirements.txt",
                runCommand = "python main.py",
                testCommand = "pytest"
            )
            ProjectType.RUST -> WorkspaceConfig(
                projectType = type,
                runCommand = "cargo run",
                testCommand = "cargo test",
                buildCommand = "cargo build --release"
            )
            ProjectType.GO -> WorkspaceConfig(
                projectType = type,
                runCommand = "go run .",
                testCommand = "go test ./...",
                buildCommand = "go build ./..."
            )
            ProjectType.C_CPP -> WorkspaceConfig(
                projectType = type,
                buildCommand = "cmake --build build",
                testCommand = "ctest --test-dir build"
            )
            ProjectType.GIT_LINKED_SYSTEM, ProjectType.GENERIC -> WorkspaceConfig(projectType = type)
        }

    /** Marker-file detection, priority ordered. First match wins. */
    fun detectType(projectRoot: File): ProjectType {
        fun has(name: String) = File(projectRoot, name).exists()
        fun hasPrefix(prefix: String) =
            projectRoot.listFiles()?.any { it.name.startsWith(prefix) && it.name.endsWith(".gradle") || it.name == prefix } == true

        return when {
            // build.gradle(.kts) at root or in a module dir + AndroidManifest anywhere
            (has("build.gradle") || has("build.gradle.kts") || hasPrefix("build.gradle")) &&
                hasAndroidManifest(projectRoot) -> ProjectType.ANDROID
            has("package.json") -> ProjectType.NODE_JS
            has("pyproject.toml") || has("requirements.txt") || has("setup.py") -> ProjectType.PYTHON
            has("Cargo.toml") -> ProjectType.RUST
            has("go.mod") -> ProjectType.GO
            has("CMakeLists.txt") || has("Makefile") -> ProjectType.C_CPP
            isGitLinkedSystem(projectRoot) -> ProjectType.GIT_LINKED_SYSTEM
            else -> ProjectType.GENERIC
        }
    }

    private fun hasAndroidManifest(root: File): Boolean {
        val direct = File(root, "app/src/main/AndroidManifest.xml")
        if (direct.exists()) return true
        // any nested AndroidManifest within depth 4
        return findManifest(root, 0)
    }

    private fun findManifest(dir: File, depth: Int): Boolean {
        if (depth > 4) return false
        val kids = dir.listFiles() ?: return false
        for (k in kids) {
            if (k.isFile && k.name == "AndroidManifest.xml") return true
            if (k.isDirectory && k.name !in setOf(".git", "build", ".gradle") &&
                findManifest(k, depth + 1)) return true
        }
        return false
    }

    /**
     * A .git directory that looks like a system repo (AOSP-style): huge tree,
     * no standard app markers. Used as the continuation of the old
     * GIT_LINKED_WORKSPACE mode.
     */
    private fun isGitLinkedSystem(root: File): Boolean {
        val git = File(root, ".git")
        if (!git.exists()) return false
        val kids = root.listFiles() ?: return false
        val dirs = kids.count { it.isDirectory }
        // multi-top-level-dir monorepo heuristics (AOSP has many)
        return dirs >= 5
    }
}
