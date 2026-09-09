package dev.anvil.ade.vcs

import dev.anvil.ade.model.DiffLine

/** Parse a unified diff into DiffLine list (pure function). */
object DiffParser {
    fun parse(raw: String): List<DiffLine> 
    if (raw.isBlank() || raw.startsWith("error")) return emptyList()
    return raw.lineSequence().map { line ->
      when {
        line.startsWith("@@") -> DiffLine(DiffLine.Type.HEADER, line)
        line.startsWith("+") && !line.startsWith("+++") ->
          DiffLine(DiffLine.Type.ADD, line.removePrefix("+"))
        line.startsWith("-") && !line.startsWith("---") ->
          DiffLine(DiffLine.Type.DELETE, line.removePrefix("-"))
        else -> DiffLine(DiffLine.Type.CONTEXT, line.removePrefix(" "))
      }
    }.toList()
  }
}
