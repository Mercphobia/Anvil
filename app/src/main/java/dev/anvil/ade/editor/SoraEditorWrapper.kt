package dev.anvil.ade.editor

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/** Editor text size in sp — sora's default (~20sp) is far too big on phones. */
private const val EDITOR_TEXT_SIZE_SP = 13f
private const val DIVIDER_COLOR = 0xFF262A35.toInt()

/**
 * Compose wrapper around sora-editor with TextMate syntax highlighting.
 *
 * Everything is dynamic: token colors are generated from the active
 * Material 3 color scheme (see [DynamicTextMateTheme]), so the editor
 * follows dark/light mode and wallpaper dynamic color. Highlighting is
 * best-effort — any failure falls back to plain text with theme colors.
 */
@Composable
fun SoraEditorWrapper(
    text: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    fileName: String? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    AndroidView(
        modifier = modifier,
        factory = { context ->
            CodeEditor(context).apply {
                setTextSize(EDITOR_TEXT_SIZE_SP)
                typefaceText = android.graphics.Typeface.MONOSPACE
                setText(text)
                editable = !readOnly
                subscribeAlways(io.github.rosemoe.sora.event.ContentChangeEvent::class.java) {
                    onTextChanged(getText().toString())
                }
            }
        },
        update = { editor ->
            applyLanguage(editor, fileName)
            applyColors(editor, colorScheme)
            val current = editor.text.toString()
            if (current != text) {
                editor.setText(text)
            }
        }
    )
}

private fun applyLanguage(editor: CodeEditor, fileName: String?) {
    try {
        val language = TextMateLanguages.languageFor(editor.context, fileName)
        if (language != null && editor.editorLanguage !== language) {
            editor.setEditorLanguage(language)
        }
    } catch (t: Throwable) {
        // keep whatever language is set
    }
}

private fun applyColors(
    editor: CodeEditor,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    // Vercel/Linear formula - the editor canvas matches the terminal canvas
    // (#0A0C10) so code and shell feel like one surface; gutter is one step
    // up with a hairline divider; line numbers stay muted.
    val background = 0xFF0A0C10.toInt()
    val textColor = 0xFFE6E8EE.toInt()
    val lineNumber = 0xFF6B7180.toInt()
    val gutterBg = 0xFF14161D.toInt()
    val dividerColor = DIVIDER_COLOR

    // Skip the rebuild when colors have not changed since the last apply -
    // this is what prevents TextMateColorScheme from being recreated (and
    // the theme from being re-parsed) on every keystroke recomposition.
    val colorKey = background xor textColor xor lineNumber xor gutterBg
    if (colorKey == lastAppliedColorKey) return
    lastAppliedColorKey = colorKey

    try {
        val themeSource = DynamicTextMateTheme.build()
        val themeRegistry = io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry.getInstance()

        // The theme MUST be registered with the ThemeRegistry and set active
        // BEFORE TextMateColorScheme.create() - the scheme reads token colors
        // from the registry, not directly from an IThemeSource. Without this,
        // token highlighting never applies (flat single color).
        val themeModel = io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel(
            themeSource, DYNAMIC_THEME_NAME
        ).apply {
            isDark = true // fixed dark palette
        }
        themeRegistry.loadTheme(themeModel)
        themeRegistry.setTheme(DYNAMIC_THEME_NAME)

        val scheme = TextMateColorScheme.create(themeRegistry)
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, background)
        scheme.setColor(EditorColorScheme.LINE_NUMBER, lineNumber)
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, gutterBg)
        scheme.setColor(EditorColorScheme.LINE_DIVIDER, dividerColor)
        scheme.setColor(EditorColorScheme.CURRENT_LINE, 0xFF191C24.toInt())
        scheme.setColor(EditorColorScheme.SELECTION_INSERT, 0xFFA4C9FF.toInt())
        scheme.setColor(EditorColorScheme.SELECTION_HANDLE, 0xFFA4C9FF.toInt())
        editor.colorScheme = scheme
    } catch (t: Throwable) {
        lastAppliedColorKey = null // allow retry on next pass
        applyFallbackScheme(editor, background, textColor, lineNumber, gutterBg)
    }
}

/** Last applied color fingerprint - module-level is fine while exactly one
 *  editor is active at a time (current app design: one file open). */
private var lastAppliedColorKey: Int? = null

private const val DYNAMIC_THEME_NAME = "anvil-dynamic"

private fun applyFallbackScheme(
    editor: CodeEditor,
    background: Int,
    textColor: Int,
    lineNumber: Int,
    gutterBg: Int
) {
    try {
        val scheme = editor.colorScheme
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, background)
        scheme.setColor(EditorColorScheme.TEXT_NORMAL, textColor)
        scheme.setColor(EditorColorScheme.LINE_NUMBER, lineNumber)
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, gutterBg)
        scheme.setColor(EditorColorScheme.LINE_DIVIDER, DIVIDER_COLOR)
    } catch (t: Throwable) {
        // theming is best-effort
    }
}
