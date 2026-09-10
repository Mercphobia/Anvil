package dev.anvil.ade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

/** Actions available on a chat message or agent step. */
@Composable
fun MessageActionToolbar(
  onCopy: () -> Unit,
  onRetry: () -> Unit,
  onDelete: () -> Unit,
  onToggleFavorite: () -> Unit = {},
  isFavorite: Boolean = false,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(AcsSurface3)
      .padding(horizontal = 4.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    ActionButton(Icons.Filled.ContentCopy, "Copy", onCopy)
    ActionButton(Icons.Filled.Refresh, "Retry", onRetry)
    ActionButton(
      if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
      "Favorite",
      onToggleFavorite
    )
    ActionButton(Icons.Filled.Delete, "Delete", onDelete)
  }
}

@Composable
private fun ActionButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .clickable(onClick = onClick)
      .padding(horizontal = 6.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      icon,
      contentDescription = label,
      tint = AcsOnSurfaceDim,
      modifier = Modifier.size(14.dp)
    )
    Spacer(Modifier.width(3.dp))
    Text(label, fontSize = 10.sp, color = AcsOnSurfaceDim)
  }
}