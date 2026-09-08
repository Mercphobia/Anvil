package com.vibe.forge.vcs

/** Parsed representation of a unified diff for the DiffViewer. */
object DiffModel {

    enum class LineKind { HEADER, ADD, REMOVE, CONTEXT, META }

    data class DiffLine(val kind: LineKind, val text: String)

    fun parse(diff: String): List<DiffLine> {
        return diff.lines().map { line ->
            when {
                line.startsWith("+++") || line.startsWith("---") -> DiffLine(LineKind.META, line)
                line.startsWith("@@") -> DiffLine(LineKind.HEADER, line)
                line.startsWith("+") -> DiffLine(LineKind.ADD, line)
                line.startsWith("-") -> DiffLine(LineKind.REMOVE, line)
                else -> DiffLine(LineKind.CONTEXT, line)
            }
        }
    }
}
