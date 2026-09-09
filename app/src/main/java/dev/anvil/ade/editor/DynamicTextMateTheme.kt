package dev.anvil.ade.editor

import org.eclipse.tm4e.core.registry.IThemeSource

/**
 * Builds Anvil's fixed dark TextMate theme (JSON) - a curated Vercel/Linear
 * style palette tuned for the editor's fixed #0A0C10 canvas. Token colors
 * do NOT follow the dynamic Material scheme: the editor, like the terminal,
 * is a deliberately constant dark surface across light/dark system modes.
 */
object DynamicTextMateTheme {

    fun build(): IThemeSource {
        // Fixed Vercel/Linear dark palette - the editor canvas no longer follows
        // the dynamic surface (it is a fixed #0A0C10), so token colors are a
        // curated dark-theme palette instead of Material mappings.
        val json = """
{
  "name": "anvil-dark",
  "type": "dark",
  "colors": {
    "editor.background": "#0A0C10",
    "editor.foreground": "#E6E8EE",
    "editor.lineHighlightBackground": "#191C24",
    "editor.selectionBackground": "#26364F",
    "editorLineNumber.foreground": "#6B7180",
    "editorCursor.foreground": "#A4C9FF"
  },
  "tokenColors": [
    { "scope": ["keyword", "storage", "storage.type", "keyword.control"],
      "settings": { "foreground": "#A4C9FF", "fontStyle": "bold" } },
    { "scope": ["string", "string.quoted", "markup.inline.raw"],
      "settings": { "foreground": "#6FD39A" } },
    { "scope": ["comment", "punctuation.definition.comment"],
      "settings": { "foreground": "#5F6572", "fontStyle": "italic" } },
    { "scope": ["constant.numeric", "constant.language", "constant.character"],
      "settings": { "foreground": "#F4C96A" } },
    { "scope": ["entity.name.type", "entity.name.class", "support.type", "support.class"],
      "settings": { "foreground": "#C9B8FF" } },
    { "scope": ["entity.name.function", "support.function", "meta.function-call"],
      "settings": { "foreground": "#82D4F5" } },
    { "scope": ["variable", "variable.parameter", "variable.other"],
      "settings": { "foreground": "#E6E8EE" } },
    { "scope": ["markup.heading", "entity.name.section"],
      "settings": { "foreground": "#A4C9FF", "fontStyle": "bold" } },
    { "scope": ["markup.bold"],
      "settings": { "fontStyle": "bold", "foreground": "#E6E8EE" } },
    { "scope": ["markup.italic"],
      "settings": { "fontStyle": "italic", "foreground": "#E6E8EE" } },
    { "scope": ["invalid", "invalid.illegal"],
      "settings": { "foreground": "#FF7A88" } }
  ]
}
""".trimIndent()
        return IThemeSource.fromString(IThemeSource.ContentType.JSON, json)
    }

}
