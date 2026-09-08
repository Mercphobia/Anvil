package com.vibe.forge.editor

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * Compose wrapper around sora-editor. Colors follow the active
 * Material 3 theme (dynamic color on Android 12+, static fallback below),
 * so the editor always matches the rest of the app.
 */
@Composable
fun SoraEditorWrapper(
    text: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
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
                applyDynamicScheme(
                    this, background, textColor, lineNumber, gutterBg,
                    keyword, comment, stringColor
                )
                setText(Content(text))
                editable = !readOnly
                subscribeAlways(io.github.rosemoe.sora.event.ContentChangeEvent::class.java) {
                    onTextChanged(getText().toString())
                }
            }
        },
        update = { editor ->
            applyDynamicScheme(
                editor, background, textColor, lineNumber, gutterBg,
                keyword, comment, stringColor
            )
            val current = editor.text.toString()
            if (current != text) {
                editor.setText(Content(text))
            }
        }
    )
}

private fun applyDynamicScheme(
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
