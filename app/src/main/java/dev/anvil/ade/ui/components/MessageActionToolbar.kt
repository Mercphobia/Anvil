package dev.anvil.ade.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.anvil.ade.ui.theme.AcsOnSurfaceDim

/**
 * Action toolbar di bawah pesan agent.
 * Row horizontal icon-only: Copy, Retry, Thumbs-up, Thumbs-down.
 */
@Composable
fun MessageActionToolbar(
    onCopy: () -> Unit,
    onRetry: () -> Unit,
    onThumbsUp: () -> Unit,
    onThumbsDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(
            onClick = onCopy,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copy response",
                tint = AcsOnSurfaceDim,
                modifier = Modifier.size(14.dp),
            )
        }

        IconButton(
            onClick = onRetry,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Retry",
                tint = AcsOnSurfaceDim,
                modifier = Modifier.size(14.dp),
            )
        }

        IconButton(
            onClick = onThumbsUp,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.ThumbUp,
                contentDescription = "Thumbs up",
                tint = AcsOnSurfaceDim,
                modifier = Modifier.size(14.dp),
            )
        }

        IconButton(
            onClick = onThumbsDown,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.ThumbDown,
                contentDescription = "Thumbs down",
                tint = AcsOnSurfaceDim,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}