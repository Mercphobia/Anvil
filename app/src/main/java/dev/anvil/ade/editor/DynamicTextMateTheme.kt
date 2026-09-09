package dev.anvil.ade.editor

import org.eclipse.tm4e.core.registry.IThemeSource

/**
 * Builds Anvil's TextMate theme (JSON) - a Vercel Geist-inspired syntax
 * palette in dark and light variants, following the system dark/light mode.
 * Dark: near-black #0A0A0A canvas. Light: pure white canvas.
 */
object DynamicTextMateTheme {

    fun build(isDark: Boolean): IThemeSource {
        // Vercel Geist-inspired syntax palette, dark & light variants.
        // Dark: near-black canvas, vivid-but-controlled tokens.
        // Light: pure white canvas, saturated 600-level tokens.
        val json = if (isDark) """
{
  "name": "anvil-vercel-dark",
  "type": "dark",
  "colors": {
    "editor.background": "#0A0A0A",
    "editor.foreground": "#EDEDED",
    "editor.lineHighlightBackground": "#1A1A1A",
    "editor.selectionBackground": "#0070F333",
    "editorLineNumber.foreground": "#666666",
    "editorCursor.foreground": "#0070F3"
  },
  "tokenColors": [
    { "scope": ["keyword", "storage", "storage.type", "keyword.control"],
      "settings": { "foreground": "#C084FC", "fontStyle": "bold" } },
    { "scope": ["string", "string.quoted", "markup.inline.raw"],
      "settings": { "foreground": "#4ADE80" } },
    { "scope": ["comment", "punctuation.definition.comment"],
      "settings": { "foreground": "#666666", "fontStyle": "italic" } },
    { "scope": ["constant.numeric", "constant.language", "constant.character"],
      "settings": { "foreground": "#FBBF24" } },
    { "scope": ["entity.name.type", "entity.name.class", "support.type", "support.class"],
      "settings": { "foreground": "#38BDF8" } },
    { "scope": ["entity.name.function", "support.function", "meta.function-call"],
      "settings": { "foreground": "#60A5FA" } },
    { "scope": ["variable", "variable.parameter", "variable.other"],
      "settings": { "foreground": "#EDEDED" } },
    { "scope": ["markup.heading", "entity.name.section"],
      "settings": { "foreground": "#C084FC", "fontStyle": "bold" } },
    { "scope": ["markup.bold"],
      "settings": { "fontStyle": "bold", "foreground": "#EDEDED" } },
    { "scope": ["markup.italic"],
      "settings": { "fontStyle": "italic", "foreground": "#EDEDED" } },
    { "scope": ["invalid", "invalid.illegal"],
      "settings": { "foreground": "#EF4444" } }
  ]
}
""".trimIndent() else """
{
  "name": "anvil-vercel-light",
  "type": "light",
  "colors": {
    "editor.background": "#FFFFFF",
    "editor.foreground": "#171717",
    "editor.lineHighlightBackground": "#F5F5F5",
    "editor.selectionBackground": "#0070F333",
    "editorLineNumber.foreground": "#A3A3A3",
    "editorCursor.foreground": "#0070F3"
  },
  "tokenColors": [
    { "scope": ["keyword", "storage", "storage.type", "keyword.control"],
      "settings": { "foreground": "#9333EA", "fontStyle": "bold" } },
    { "scope": ["string", "string.quoted", "markup.inline.raw"],
      "settings": { "foreground": "#16A34A" } },
    { "scope": ["comment", "punctuation.definition.comment"],
      "settings": { "foreground": "#A3A3A3", "fontStyle": "italic" } },
    { "scope": ["constant.numeric", "constant.language", "constant.character"],
      "settings": { "foreground": "#D97706" } },
    { "scope": ["entity.name.type", "entity.name.class", "support.type", "support.class"],
      "settings": { "foreground": "#0284C7" } },
    { "scope": ["entity.name.function", "support.function", "meta.function-call"],
      "settings": { "foreground": "#2563EB" } },
    { "scope": ["variable", "variable.parameter", "variable.other"],
      "settings": { "foreground": "#171717" } },
    { "scope": ["markup.heading", "entity.name.section"],
      "settings": { "foreground": "#9333EA", "fontStyle": "bold" } },
    { "scope": ["markup.bold"],
      "settings": { "fontStyle": "bold", "foreground": "#171717" } },
    { "scope": ["markup.italic"],
      "settings": { "fontStyle": "italic", "foreground": "#171717" } },
    { "scope": ["invalid", "invalid.illegal"],
      "settings": { "foreground": "#DC2626" } }
  ]
}
""".trimIndent()
        return IThemeSource.fromString(IThemeSource.ContentType.JSON, json)
    }
}
