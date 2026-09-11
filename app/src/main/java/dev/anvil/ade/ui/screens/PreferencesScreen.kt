package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.components.SettingsCardGroup
import dev.anvil.ade.ui.components.SettingsCardRow
import dev.anvil.ade.ui.components.SettingsSectionLabel

@Composable
fun PreferencesScreen(
  onBack: () -> Unit,
  onOpenGeneral: () -> Unit = {}, onOpenEditor: () -> Unit = {}, onOpenAiAgent: () -> Unit = {},
  onOpenBuildRun: () -> Unit = {}, onOpenTermux: () -> Unit = {}, onOpenPrivacy: () -> Unit = {},
  onOpenDevOptions: () -> Unit = {}, onOpenAbout: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface) }
      Text("IDE Preferences", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Column(Modifier.weight(1f).padding(16.dp)) {
      SettingsSectionLabel("Configure")
      SettingsCardGroup {
        SettingsCardRow(Icons.Filled.Settings, "General", "General IDE configuration", onClick = onOpenGeneral)
        SettingsCardRow(Icons.Filled.Code, "Editor", "Configure the editor", onClick = onOpenEditor)
        SettingsCardRow(Icons.Filled.Psychology, "AI Agent", "AI-powered code generation", onClick = onOpenAiAgent)
        SettingsCardRow(Icons.Filled.Build, "Build & Run", "Configure Gradle build", onClick = onOpenBuildRun)
        SettingsCardRow(Icons.Filled.Terminal, "Termux", "Terminal preferences", onClick = onOpenTermux)
      }
      Spacer(Modifier.height(16.dp))
      SettingsSectionLabel("System")
      SettingsCardGroup {
        SettingsCardRow(Icons.Filled.Security, "Privacy", "Privacy & data settings", onClick = onOpenPrivacy)
      }
      Spacer(Modifier.height(16.dp))
      SettingsSectionLabel("More")
      SettingsCardGroup {
        SettingsCardRow(Icons.Filled.Tune, "Developer options", "Experimental options", onClick = onOpenDevOptions)
        SettingsCardRow(Icons.Filled.Info, "About", "About Anvil", onClick = onOpenAbout)
      }
    }
  }
}