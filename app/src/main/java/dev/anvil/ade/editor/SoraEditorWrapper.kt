package dev.anvil.ade.editor

import androidx.compose.foundation.isSystemInDarkTheme
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
    val isDark = isSystemInDarkTheme()

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
            applyColors(editor, colorScheme, isDark)
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
    colorScheme: androidx.compose.material3.ColorScheme,
    isDark: Boolean
) {
    // Vercel Geist: near-black canvas in dark mode, pure white in light.
    // Gutter matches canvas; hairline divider; muted line numbers.
    val background = if (isDark) 0xFF0A0A0A.toInt() else 0xFFFFFFFF.toInt()
    val textColor = if (isDark) 0xFFEDEDED.toInt() else 0xFF171717.toInt()
    val lineNumber = if (isDark) 0xFF666666.toInt() else 0xFFA3A3A3.toInt()
    val gutterBg = background
    val dividerColor = if (isDark) 0xFF262626.toInt() else 0xFFEAEAEA.toInt()

    // Skip the rebuild when colors have not changed since the last apply -
    // this is what prevents TextMateColorScheme from being recreated (and
    // the theme from being re-parsed) on every keystroke recomposition.
    val colorKey = background xor textColor xor lineNumber xor gutterBg
    if (colorKey == lastAppliedColorKey) return
    lastAppliedColorKey = colorKey

    try {
        val themeSource = DynamicTextMateTheme.build(isDark)
        val themeRegistry = io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry.getInstance()

        // The theme MUST be registered with the ThemeRegistry and set active
        // BEFORE TextMateColorScheme.create() - the scheme reads token colors
        // from the registry, not directly from an IThemeSource. Without this,
        // token highlighting never applies (flat single color).
        val themeModel = io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel(
            themeSource, DYNAMIC_THEME_NAME
        ).apply {
            this.isDark = isDark
        }
        themeRegistry.loadTheme(themeModel)
        themeRegistry.setTheme(DYNAMIC_THEME_NAME)

        // CRITICAL: TextMateColorScheme's constructor only ASSIGNS the theme
        // model - it does not load colors (rawTheme stays null until
        // setTheme()/applyDefault() runs). Creating the scheme via the static
        // factory right after registry.setTheme() misses the theme-change
        // dispatch, so the scheme would render with fallback flat colors.
        // Explicitly loading the theme into the scheme instance fixes it.
        val scheme = TextMateColorScheme.create(themeRegistry, themeModel)
        scheme.setTheme(themeModel)
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, background)
        scheme.setColor(EditorColorScheme.LINE_NUMBER, lineNumber)
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, gutterBg)
        scheme.setColor(EditorColorScheme.LINE_DIVIDER, dividerColor)
        scheme.setColor(EditorColorScheme.CURRENT_LINE, if (isDark) 0xFF1A1A1A.toInt() else 0xFFF5F5F5.toInt())
        scheme.setColor(EditorColorScheme.SELECTION_INSERT, 0xFF0070F3.toInt())
        scheme.setColor(EditorColorScheme.SELECTION_HANDLE, 0xFF0070F3.toInt())
        editor.colorScheme = scheme
    } catch (t: Throwable) {
        lastAppliedColorKey = null // allow retry on next pass
        applyFallbackScheme(editor, background, textColor, lineNumber, gutterBg, dividerColor)
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
    gutterBg: Int,
    dividerColor: Int
) {
    try {
        val scheme = editor.colorScheme
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, background)
        scheme.setColor(EditorColorScheme.TEXT_NORMAL, textColor)
        scheme.setColor(EditorColorScheme.LINE_NUMBER, lineNumber)
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, gutterBg)
        scheme.setColor(EditorColorScheme.LINE_DIVIDER, dividerColor)
    } catch (t: Throwable) {
        // theming is best-effort
    }
}
