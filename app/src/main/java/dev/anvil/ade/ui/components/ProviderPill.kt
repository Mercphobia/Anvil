package dev.anvil.ade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.model.ProjectType

/** Pill badge showing the active project type with an icon. */
@Composable
fun ProviderPill(
  projectType: ProjectType,
  modifier: Modifier = Modifier
) {
  val (icon, labelColor) = when (projectType) {
    ProjectType.ANDROID -> Icons.Filled.Android to MaterialTheme.colorScheme.primary
    ProjectType.NODE_JS -> Icons.Filled.Code to MaterialTheme.colorScheme.secondary
    ProjectType.PYTHON -> Icons.Filled.Code to MaterialTheme.colorScheme.secondary
    ProjectType.RUST -> Icons.Filled.Build to MaterialTheme.colorScheme.error
    ProjectType.GO -> Icons.Filled.Code to MaterialTheme.colorScheme.secondary
    ProjectType.C_CPP -> Icons.Filled.Terminal to MaterialTheme.colorScheme.onSurfaceVariant
    ProjectType.GIT_LINKED_SYSTEM -> Icons.Filled.Folder to MaterialTheme.colorScheme.primary
    ProjectType.GENERIC -> Icons.Filled.InsertDriveFile to MaterialTheme.colorScheme.onSurfaceVariant
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(labelColor.copy(alpha = 0.12f))
      .border(0.5.dp, labelColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(icon, contentDescription = null, tint = labelColor, modifier = Modifier.size(14.dp))
    Spacer(Modifier.width(5.dp))
    Text(text = projectType.displayName, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = labelColor)
  }
}