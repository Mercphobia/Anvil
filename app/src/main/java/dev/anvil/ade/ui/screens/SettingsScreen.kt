package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.components.AcsCardGroup
import dev.anvil.ade.ui.components.AcsCardRow
import dev.anvil.ade.ui.components.AcsCardSwitchRow
import dev.anvil.ade.ui.components.AcsSectionLabel

@Composable
fun SettingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
  var darkTheme by remember { mutableStateOf(true) }
  var autonomyLevel by remember { mutableStateOf(0) }
  var mcpEnabled by remember { mutableStateOf(false) }
  var hooksEnabled by remember { mutableStateOf(false) }
  val autonomyLabels = listOf("Safe (ask always)", "Moderate", "Full auto")

  Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface) }
      Text("Settings", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Column(Modifier.weight(1f).padding(16.dp)) {
      AcsSectionLabel("Appearance")
      AcsCardGroup {
        AcsCardSwitchRow(Icons.Filled.DarkMode, "Dark theme", "Use dark color scheme", darkTheme, { darkTheme = !darkTheme })
      }
      Spacer(Modifier.height(16.dp))
      AcsSectionLabel("Autonomy")
      AcsCardGroup {
        AcsCardRow(Icons.Filled.AutoAwesome, "Autonomy level", autonomyLabels[autonomyLevel], onClick = { autonomyLevel = (autonomyLevel + 1) % 3 })
      }
      Spacer(Modifier.height(16.dp))
      AcsSectionLabel("Services")
      AcsCardGroup {
        AcsCardSwitchRow(Icons.Filled.Hub, "MCP Server", "Expose tools via HTTP", mcpEnabled, { mcpEnabled = !mcpEnabled })
        AcsCardSwitchRow(Icons.Filled.Link, "Hook Engine", "File event automation", hooksEnabled, { hooksEnabled = !hooksEnabled })
      }
    }
  }
}