package com.vibe.forge.editor

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import org.eclipse.tm4e.core.registry.IThemeSource

/**
 * Compose wrapper around sora-editor with TextMate syntax highlighting.
 *
 * Base editor colors (background, gutter, line numbers) follow the active
 * Material 3 theme; token colors (keywords, strings, comments, ...) come
 * from the bundled Quiet Light TextMate theme via [TextMateLanguages].
 * Highlighting is best-effort: any failure falls back to plain text.
 */
@Composable
fun SoraEditorWrapper(
    text: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    fileName: String? = null
) {
    val background = MaterialTheme.colorScheme.surfaceContainerLowest.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val lineNumber = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val gutterBg = MaterialTheme.colorScheme.surfaceContainerLow.toArgb()
    val keyword = MaterialTheme.colorScheme.primary.toArgb()
    val comment = MaterialTheme.colorScheme.outline.toArgb()
    val stringColor = MaterialTheme.colorScheme.tertiary.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            CodeEditor(context).apply {
                applyHighlighting(this, context, fileName, background, textColor,
                    lineNumber, gutterBg, keyword, comment, stringColor)
                setText(text)
                editable = !readOnly
                subscribeAlways(io.github.rosemoe.sora.event.ContentChangeEvent::class.java) {
                    onTextChanged(getText().toString())
                }
            }
        },
        update = { editor ->
            applyHighlighting(editor, editor.context, fileName, background, textColor,
                lineNumber, gutterBg, keyword, comment, stringColor)
            val current = editor.text.toString()
            if (current != text) {
                editor.setText(text)
            }
        }
    )
}

private fun applyHighlighting(
    editor: CodeEditor,
    context: android.content.Context,
    fileName: String?,
    background: Int,
    textColor: Int,
    lineNumber: Int,
    gutterBg: Int,
    keyword: Int,
    comment: Int,
    stringColor: Int
) {
    // 1. Language by file extension (null = plain text fallback)
    try {
        val language = TextMateLanguages.languageFor(context, fileName)
        if (language != null && editor.editorLanguage !== language) {
            editor.setEditorLanguage(language)
        }
    } catch (t: Throwable) {
        // keep whatever language is set
    }

    // 2. Colors: TextMate theme for tokens, Material scheme for the chrome
    try {
        val scheme = TextMateColorScheme.create(
            IThemeSource.fromInputStream(
                context.assets.open("textmate/quietlight.json"),
                "quietlight.json",
                null
            )
        )
        // Overwrite chrome colors with the app theme so the editor still
        // matches the rest of the UI (dark/light, dynamic color).
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, background)
        scheme.setColor(EditorColorScheme.LINE_NUMBER, lineNumber)
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, gutterBg)
        scheme.setColor(EditorColorScheme.LINE_DIVIDER, gutterBg)
        editor.colorScheme = scheme
    } catch (t: Throwable) {
        applyFallbackScheme(editor, background, textColor, lineNumber,
            gutterBg, keyword, comment, stringColor)
    }
}

private fun applyFallbackScheme(
    editor: CodeEditor,
    background: Int,
    textColor: Int,
    lineNumber: Int,
    gutterBg: Int,
    keyword: Int,
    comment: Int,
    stringColor: Int
) {
    try {
        val scheme = editor.colorScheme
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, background)
        scheme.setColor(EditorColorScheme.TEXT_NORMAL, textColor)
        scheme.setColor(EditorColorScheme.LINE_NUMBER, lineNumber)
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, gutterBg)
        scheme.setColor(EditorColorScheme.KEYWORD, keyword)
        scheme.setColor(EditorColorScheme.COMMENT, comment)
        scheme.setColor(EditorColorScheme.LITERAL, stringColor)
    } catch (t: Throwable) {
        // theming is best-effort
    }
}
