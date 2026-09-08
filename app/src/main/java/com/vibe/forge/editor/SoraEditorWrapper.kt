package com.vibe.forge.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

/**
 * Compose wrapper around sora-editor with the Forge dark scheme.
 * Grammar for Kotlin/Java/XML is loaded by the caller's file type in
 * later phases; the base view is language-agnostic here.
 */
@Composable
fun SoraEditorWrapper(
    text: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            CodeEditor(context).apply {
                applyForgeScheme(this)
                setText(Content(text))
                editable = !readOnly
                subscribeAlways(io.github.rosemoe.sora.event.ContentChangeEvent::class.java) {
                    onTextChanged(getText().toString())
                }
            }
        },
        update = { editor ->
            val current = editor.text.toString()
            if (current != text) {
                editor.setText(Content(text))
            }
        }
    )
}

private fun applyForgeScheme(editor: CodeEditor) {
    try {
        val scheme = editor.colorScheme
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, 0xFF141518.toInt())
        scheme.setColor(EditorColorScheme.TEXT_NORMAL, 0xFFE2E4E9.toInt())
        scheme.setColor(EditorColorScheme.LINE_NUMBER, 0xFF9BA0A8.toInt())
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, 0xFF1C1E22.toInt())
        scheme.setColor(EditorColorScheme.KEYWORD, 0xFF4F8CFF.toInt())
        scheme.setColor(EditorColorScheme.COMMENT, 0xFF6B7078.toInt())
    } catch (t: Throwable) {
        // theming is best-effort
    }
}
