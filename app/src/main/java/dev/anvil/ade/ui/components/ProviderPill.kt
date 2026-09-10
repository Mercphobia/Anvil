package dev.anvil.ade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.model.ProjectType
import dev.anvil.ade.ui.theme.*

/** Pill badge showing the active project type with an icon. */
@Composable
fun ProviderPill(
  projectType: ProjectType,
  modifier: Modifier = Modifier
) {
  val (icon, color) = when (projectType) {
    ProjectType.ANDROID -> Icons.Default.Android to AcsFileIconKt
    ProjectType.NODE_JS -> Icons.Default.Code to AcsGreen
    ProjectType.PYTHON -> Icons.Default.Code to AcsTeal
    ProjectType.RUST -> Icons.Default.Build to AcsRed
    ProjectType.GO -> Icons.Default.Code to AcsTeal
    ProjectType.C_CPP -> Icons.Default.Terminal to AcsOnSurfaceVariant
    ProjectType.GIT_LINKED_SYSTEM -> Icons.Default.Folder to AcsFolderIcon
    ProjectType.GENERIC -> Icons.Default.InsertDriveFile to AcsOnSurfaceDim
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(color.copy(alpha = 0.12f))
      .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      icon,
      contentDescription = null,
      tint = color,
      modifier = Modifier.size(14.dp)
    )
    Spacer(Modifier.width(5.dp))
    Text(
      text = projectType.displayName,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = color
    )
  }
}