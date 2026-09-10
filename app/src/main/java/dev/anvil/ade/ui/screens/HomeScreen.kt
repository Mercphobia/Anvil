package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

@Composable
fun HomeScreen(
  onCreateProject: () -> Unit,
  onOpenProject: () -> Unit,
  onCloneGit: () -> Unit,
  onOpenTerminal: () -> Unit,
  onOpenPreferences: () -> Unit,
  onOpenIdeConfig: () -> Unit,
  onOpenDocs: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AcsBg)
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text("ACS", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = AcsGold, letterSpacing = (-2).sp)
    Spacer(Modifier.height(4.dp))
    Text("Android Code Studio", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AcsOnSurface)
    Spacer(Modifier.height(2.dp))
    Text("Your Ideas, Anywhere", fontSize = 14.sp, color = AcsGold)
    Spacer(Modifier.height(36.dp))

    val actions = listOf(
      Triple("Create project", "Start your new awesome project!", Icons.Filled.Add) to onCreateProject,
      Triple("Open existing project", "Import or browse for a project", Icons.Filled.FolderOpen) to onOpenProject,
      Triple("Clone git repository", "Check out code from a remote repository", Icons.Filled.CallSplit) to onCloneGit,
      Triple("Terminal", "Open a terminal session", Icons.Filled.Terminal) to onOpenTerminal,
      Triple("Preferences", "Configure IDE settings", Icons.Filled.Settings) to onOpenPreferences,
      Triple("IDE Configurations", "IDE build system configurations", Icons.Filled.Tune) to onOpenIdeConfig,
      Triple("Documentation", "Learn more about Android Code Studio", Icons.Filled.MenuBook) to onOpenDocs
    )

    Column(Modifier.fillMaxWidth()) {
      actions.forEachIndexed { idx, (triple, onClick) ->
        val (title, subtitle, icon) = triple
        Row(
          Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(AcsSurface2).clickable(onClick = onClick)
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AcsGold.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = AcsGold, modifier = Modifier.size(18.dp))
          }
          Spacer(Modifier.width(12.dp))
          Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AcsOnSurface)
            Text(subtitle, fontSize = 12.sp, color = AcsOnSurfaceVariant)
          }
        }
        if (idx < actions.lastIndex) Spacer(Modifier.height(8.dp))
      }
    }
  }
}