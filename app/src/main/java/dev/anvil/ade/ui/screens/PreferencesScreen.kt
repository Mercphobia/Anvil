package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

@Composable
fun PreferencesScreen(
  onBack: () -> Unit,
  onOpenGeneral: () -> Unit = {},
  onOpenEditor: () -> Unit = {},
  onOpenAiAgent: () -> Unit = {},
  onOpenBuildRun: () -> Unit = {},
  onOpenTermux: () -> Unit = {},
  onOpenPrivacy: () -> Unit = {},
  onOpenDevOptions: () -> Unit = {},
  onOpenAbout: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Text("IDE Preferences", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    Spacer(Modifier.height(4.dp))

    SectionHeader("Configure")
    PrefsRow("General", "General IDE configuration", Icons.Filled.Settings, onClick = onOpenGeneral)
    PrefsRow("Editor", "Configure the editor", Icons.Filled.Code, onClick = onOpenEditor)
    PrefsRow("AI Agent", "Get AI-powered code generation using AI Agent", Icons.Filled.Psychology, onClick = onOpenAiAgent, accent = AcsTeal)
    PrefsRow("Build & Run", "Configure the Gradle build", Icons.Filled.Build, onClick = onOpenBuildRun)
    PrefsRow("Termux", "Preferences for the Termux terminal", Icons.Filled.Terminal, onClick = onOpenTermux)

    SectionHeader("System")
    PrefsRow("Privacy", "Privacy & data settings", Icons.Filled.Security, onClick = onOpenPrivacy)

    SectionHeader("More")
    PrefsRow("Developer options", "Developer options", Icons.Filled.Tune, onClick = onOpenDevOptions, subtitle = "Experimental/debugging options for AndroidCS")
    PrefsRow("About", "About Android Code Studio", Icons.Filled.Info, onClick = onOpenAbout, subtitle = "More about Android Code Studio")
  }
}

@Composable
private fun SectionHeader(title: String) {
  Text(
    title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
    color = AcsGold, letterSpacing = 1.sp,
    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 6.dp)
  )
}

@Composable
private fun PrefsRow(
  title: String, subtitle: String?, icon: ImageVector,
  onClick: () -> Unit, accent: androidx.compose.ui.graphics.Color = AcsGold,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(0.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 20.dp, vertical = 13.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp))
        .background(accent.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, null, tint = accent, modifier = Modifier.size(17.dp))
    }
    Spacer(Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
      subtitle?.let {
        Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
      }
    }
    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
  }
}