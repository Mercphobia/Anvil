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
import dev.anvil.ade.ui.components.AcsCardGroup
import dev.anvil.ade.ui.components.AcsCardRow
import dev.anvil.ade.ui.components.AcsSectionLabel
import dev.anvil.ade.ui.theme.*

@Composable
fun PreferencesScreen(
  onBack: () -> Unit,
  onOpenGeneral: () -> Unit = {}, onOpenEditor: () -> Unit = {}, onOpenAiAgent: () -> Unit = {},
  onOpenBuildRun: () -> Unit = {}, onOpenTermux: () -> Unit = {}, onOpenPrivacy: () -> Unit = {},
  onOpenDevOptions: () -> Unit = {}, onOpenAbout: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Column(modifier.fillMaxSize().background(AcsBg)) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = AcsOnSurface) }
      Text("IDE Preferences", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AcsOnSurface)
    }
    HorizontalDivider(color = AcsOutlineVariant)
    Column(Modifier.weight(1f).padding(16.dp)) {
      AcsSectionLabel("Configure")
      AcsCardGroup {
        AcsCardRow(Icons.Filled.Settings, "General", "General IDE configuration", onClick = onOpenGeneral)
        AcsCardRow(Icons.Filled.Code, "Editor", "Configure the editor", onClick = onOpenEditor)
        AcsCardRow(Icons.Filled.Psychology, "AI Agent", "AI-powered code generation", onClick = onOpenAiAgent)
        AcsCardRow(Icons.Filled.Build, "Build & Run", "Configure Gradle build", onClick = onOpenBuildRun)
        AcsCardRow(Icons.Filled.Terminal, "Termux", "Terminal preferences", onClick = onOpenTermux)
      }
      Spacer(Modifier.height(16.dp))
      AcsSectionLabel("System")
      AcsCardGroup {
        AcsCardRow(Icons.Filled.Security, "Privacy", "Privacy & data settings", onClick = onOpenPrivacy)
      }
      Spacer(Modifier.height(16.dp))
      AcsSectionLabel("More")
      AcsCardGroup {
        AcsCardRow(Icons.Filled.Tune, "Developer options", "Experimental options", onClick = onOpenDevOptions)
        AcsCardRow(Icons.Filled.Info, "About", "About Android Code Studio", onClick = onOpenAbout)
      }
    }
  }
}