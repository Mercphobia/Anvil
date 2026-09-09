package dev.anvil.ade.workspace

import dev.anvil.ade.model.ProjectFile

/** Pure helpers to build a ProjectFile tree from a real directory. */
object WorkspaceTree {
    fun buildFromDisk(root: java.io.File, maxDepth: Int = 6): List<ProjectFile> {
    fun walk(dir: java.io.File, depth: Int): List<ProjectFile> {
      if (depth > maxDepth) return emptyList()
      val entries = dir.listFiles() ?: return emptyList()
      return entries
        .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        .mapNotNull { f ->
          try {
            if (f.isDirectory) {
              ProjectFile(
                name = f.name,
                path = f.absolutePath,
                isDirectory = true,
                children = walk(f, depth + 1)
              )
            } else {
              val text = if (f.length() < 500_000) f.readText()
              else "[file terlalu besar untuk ditampilkan: ${f.length()} bytes]"
              ProjectFile(
                name = f.name,
                path = f.absolutePath,
                isDirectory = false,
                content = text,
                language = f.extension.ifBlank { "txt" }
              )
            }
          } catch (t: Throwable) { null }
        }
    }
    return walk(root, 0)
  }

    fun findFirstFile(node: ProjectFile): ProjectFile? {
    if (!node.isDirectory) return node
    for (child in node.children) {
      findFirstFile(child)?.let { return it }
    }
    return null
  }
}
