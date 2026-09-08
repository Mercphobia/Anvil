package com.vibe.forge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibe.forge.vcs.DiffModel

@Composable
fun DiffViewer(
    diff: String,
    modifier: Modifier = Modifier
) {
    val lines = DiffModel.parse(diff)

    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(lines) { line ->
            val (bg, fg) = when (line.kind) {
                DiffModel.LineKind.ADD ->
                    Color(0xFF1B3A24) to Color(0xFF7BD88F)
                DiffModel.LineKind.REMOVE ->
                    Color(0xFF3A1B1B) to Color(0xFFE5534B)
                DiffModel.LineKind.HEADER ->
                    Color(0xFF1B2A3A) to Color(0xFF4F8CFF)
                DiffModel.LineKind.META ->
                    Color.Transparent to MaterialTheme.colorScheme.onSurfaceVariant
                DiffModel.LineKind.CONTEXT ->
                    Color.Transparent to MaterialTheme.colorScheme.onSurface
            }
            Text(
                line.text,
                color = fg,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(horizontal = 8.dp, vertical = 1.dp)
            )
        }
    }
}
