package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*
import dev.anvil.ade.ui.components.AcsCardGroup
import dev.anvil.ade.ui.components.AcsCardRow
import dev.anvil.ade.ui.components.AcsSectionLabel

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

    AcsSectionLabel("Configure", modifier = Modifier.padding(horizontal = 16.dp))
    AcsCardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
      AcsCardRow(Icons.Filled.Settings, "General", "General IDE configuration", onClick = onOpenGeneral)
      AcsCardRow(Icons.Filled.Code, "Editor", "Configure the editor", onClick = onOpenEditor)
      AcsCardRow(Icons.Filled.Psychology, "AI Agent", "Get AI-powered code generation using AI Agent", onClick = onOpenAiAgent, accent = AcsTeal)
      AcsCardRow(Icons.Filled.Build, "Build & Run", "Configure the Gradle build", onClick = onOpenBuildRun)
      AcsCardRow(Icons.Filled.Terminal, "Termux", "Preferences for the Termux terminal", onClick = onOpenTermux)
    }

    AcsSectionLabel("System", modifier = Modifier.padding(horizontal = 16.dp))
    AcsCardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
      AcsCardRow(Icons.Filled.Security, "Privacy", "Privacy & data settings", onClick = onOpenPrivacy)
    }

    AcsSectionLabel("More", modifier = Modifier.padding(horizontal = 16.dp))
    AcsCardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
      AcsCardRow(Icons.Filled.Tune, "Developer options", "Experimental/debugging options for AndroidCS", onClick = onOpenDevOptions)
      AcsCardRow(Icons.Filled.Info, "About", "More about Android Code Studio", onClick = onOpenAbout)
    }
  }
}