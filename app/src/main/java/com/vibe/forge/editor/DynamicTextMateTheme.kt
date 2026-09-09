package com.vibe.forge.editor

import android.graphics.Color
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import org.eclipse.tm4e.core.registry.IThemeSource

/**
 * Builds a TextMate theme (JSON) whose token colors are derived from the
 * app's Material 3 color scheme, so syntax highlighting follows dark/light
 * mode and wallpaper-based dynamic color like the rest of the UI.
 *
 * Role mapping (kept readable on both light and dark schemes):
 *   keyword / storage      -> primary
 *   string                 -> tertiary
 *   comment                -> outline (italic)
 *   number / constant      -> secondary
 *   type / class           -> inversePrimary
 *   function               -> primaryContainer-ish onSurface tint
 *   variable / parameter   -> onSurface
 *   invalid                -> error
 *   editor background      -> surfaceContainerLowest
 *   foreground             -> onSurface
 *   line highlight         -> surfaceContainerLow
 *   selection              -> secondaryContainer (55% alpha)
 *   line numbers           -> onSurfaceVariant
 */
object DynamicTextMateTheme {

    fun build(cs: ColorScheme): IThemeSource {
        val bg = cs.surfaceContainerLowest.toArgb()
        val json = """
{
  "name": "vibeforge-dynamic",
  "type": "${if (isLight(cs)) "light" else "dark"}",
  "colors": {
    "editor.background": "${hex(bg)}",
    "editor.foreground": "${hex(cs.onSurface.toArgb())}",
    "editor.lineHighlightBackground": "${hex(cs.surfaceContainerLow.toArgb())}",
    "editor.selectionBackground": "${hexAlpha(cs.secondaryContainer.toArgb(), 0x88)}",
    "editorLineNumber.foreground": "${hex(cs.onSurfaceVariant.toArgb())}",
    "editorCursor.foreground": "${hex(cs.primary.toArgb())}"
  },
  "tokenColors": [
    {
      "scope": ["keyword", "storage", "storage.type", "keyword.control"],
      "settings": { "foreground": "${hex(cs.primary.toArgb())}", "fontStyle": "bold" }
    },
    {
      "scope": ["string", "string.quoted", "markup.inline.raw"],
      "settings": { "foreground": "${hex(cs.tertiary.toArgb())}" }
    },
    {
      "scope": ["comment", "punctuation.definition.comment"],
      "settings": { "foreground": "${hex(cs.outline.toArgb())}", "fontStyle": "italic" }
    },
    {
      "scope": ["constant.numeric", "constant.language", "constant.character"],
      "settings": { "foreground": "${hex(cs.secondary.toArgb())}" }
    },
    {
      "scope": ["entity.name.type", "entity.name.class", "support.type", "support.class"],
      "settings": { "foreground": "${hex(cs.inversePrimary.toArgb())}" }
    },
    {
      "scope": ["entity.name.function", "support.function", "meta.function-call"],
      "settings": { "foreground": "${hex(cs.primary.toArgb())}" }
    },
    {
      "scope": ["variable", "variable.parameter", "variable.other"],
      "settings": { "foreground": "${hex(cs.onSurface.toArgb())}" }
    },
    {
      "scope": ["markup.heading", "entity.name.section"],
      "settings": { "foreground": "${hex(cs.primary.toArgb())}", "fontStyle": "bold" }
    },
    {
      "scope": ["markup.bold"],
      "settings": { "fontStyle": "bold", "foreground": "${hex(cs.onSurface.toArgb())}" }
    },
    {
      "scope": ["markup.italic"],
      "settings": { "fontStyle": "italic", "foreground": "${hex(cs.onSurface.toArgb())}" }
    },
    {
      "scope": ["invalid", "invalid.illegal"],
      "settings": { "foreground": "${hex(cs.error.toArgb())}" }
    }
  ]
}
""".trimIndent()
        return IThemeSource.fromString(IThemeSource.ContentType.JSON, json)
    }

    private fun isLight(cs: ColorScheme): Boolean {
        val c = cs.surfaceContainerLowest.toArgb()
        val lum = (0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c)) / 255.0
        return lum > 0.5
    }

    /** Whether [cs] reads as a light scheme - internal so SoraEditorWrapper
     *  can flag the registered ThemeModel as light/dark. */
    internal fun isLightScheme(cs: ColorScheme): Boolean = isLight(cs)

    private fun hex(argb: Int): String = String.format("#%06X", 0xFFFFFF and argb)

    private fun hexAlpha(argb: Int, alpha: Int): String =
        String.format("#%08X", (alpha shl 24) or (0xFFFFFF and argb))
}
