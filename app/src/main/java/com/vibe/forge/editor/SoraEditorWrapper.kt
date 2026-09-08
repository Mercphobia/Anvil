package com.vibe.forge.editor

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
    val background = colorScheme.surfaceContainerLowest.toArgb()
    val textColor = colorScheme.onSurface.toArgb()
    val lineNumber = colorScheme.onSurfaceVariant.toArgb()
    val gutterBg = colorScheme.surfaceContainerLow.toArgb()
    try {
        val scheme = TextMateColorScheme.create(DynamicTextMateTheme.build(colorScheme))
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, background)
        scheme.setColor(EditorColorScheme.LINE_NUMBER, lineNumber)
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, gutterBg)
        scheme.setColor(EditorColorScheme.LINE_DIVIDER, gutterBg)
        editor.colorScheme = scheme
    } catch (t: Throwable) {
        applyFallbackScheme(editor, background, textColor, lineNumber, gutterBg)
    }
}

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
    } catch (t: Throwable) {
        // theming is best-effort
    }
}
