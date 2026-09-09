package dev.anvil.ade.model

/**
 * Universal project type - replaces the old two-mode (MODE_A/MODE_B) model.
 * Detected from marker files at the project root (see WorkspaceDetector);
 * the type decides which build pipeline, skills, and editor behaviors apply.
 *
 * Roadmap: ANDROID has a native in-process pipeline; everything else runs
 * through the generic shell pipeline via the embedded Unix environment.
 */
enum class ProjectType(val displayName: String) {
    ANDROID("Android App"),
    NODE_JS("Node.js"),
    PYTHON("Python"),
    RUST("Rust"),
    GO("Go"),
    C_CPP("C/C++"),
    GIT_LINKED_SYSTEM("Git-Linked System Repo"),
    GENERIC("Generic Project");

    /** Projects that run through the Android-native build pipeline. */
    fun isAndroid(): Boolean = this == ANDROID

    companion object {
        fun byId(id: String): ProjectType = entries.firstOrNull { it.name == id } ?: GENERIC
    }
}
