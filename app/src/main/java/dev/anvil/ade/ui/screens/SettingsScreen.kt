package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.components.AcsCardGroup
import dev.anvil.ade.ui.components.AcsCardRow
import dev.anvil.ade.ui.components.AcsCardSwitchRow
import dev.anvil.ade.ui.components.AcsSectionLabel
import dev.anvil.ade.ui.theme.*

/** Settings screen: theme, autonomy, and API configuration toggles. */
@Composable
fun SettingsScreen(
  darkTheme: Boolean,
  amoledBlack: Boolean,
  autonomousMode: Boolean,
  agentConfirmBeforeWrite: Boolean,
  showFileIcons: Boolean,
  onToggleDarkTheme: (Boolean) -> Unit,
  onToggleAmoledBlack: (Boolean) -> Unit,
  onToggleAutonomousMode: (Boolean) -> Unit,
  onToggleConfirmBeforeWrite: (Boolean) -> Unit,
  onToggleShowFileIcons: (Boolean) -> Unit,
  onOpenProviderSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AcsBg)
      .verticalScroll(rememberScrollState())
      .padding(bottom = 32.dp)
  ) {
    AcsSectionLabel("APPEARANCE")

    AcsCardGroup {
      AcsCardSwitchRow(
        icon = Icons.Default.DarkMode,
        title = "Dark Theme",
        subtitle = if (darkTheme) "Always dark" else "Follow system",
        checked = darkTheme,
        onCheckedChange = onToggleDarkTheme
      )
      AcsCardSwitchRow(
        icon = Icons.Default.PhoneAndroid,
        title = "AMOLED Black",
        subtitle = "Deeper blacks on OLED screens",
        checked = amoledBlack,
        onCheckedChange = onToggleAmoledBlack
      )
      AcsCardSwitchRow(
        icon = Icons.Default.InsertDriveFile,
        title = "Show File Icons",
        subtitle = "Color-coded icons in file tree",
        checked = showFileIcons,
        onCheckedChange = onToggleShowFileIcons
      )
    }

    Spacer(Modifier.height(8.dp))

    AcsSectionLabel("AI AGENT")

    AcsCardGroup {
      AcsCardSwitchRow(
        icon = Icons.Default.AutoAwesome,
        title = "Autonomous Mode",
        subtitle = "Agent acts without confirmation",
        checked = autonomousMode,
        onCheckedChange = onToggleAutonomousMode
      )
      AcsCardSwitchRow(
        icon = Icons.Default.Edit,
        title = "Confirm Before Write",
        subtitle = "Ask before editing files",
        checked = agentConfirmBeforeWrite,
        onCheckedChange = onToggleConfirmBeforeWrite
      )
    }

    Spacer(Modifier.height(8.dp))

    AcsSectionLabel("LLM PROVIDER")

    AcsCardGroup {
      AcsCardRow(
        icon = Icons.Default.Api,
        title = "Provider Settings",
        subtitle = "API key, model, endpoint",
        onClick = onOpenProviderSettings
      )
    }

    Spacer(Modifier.height(8.dp))

    AcsSectionLabel("ABOUT")

    AcsCardGroup {
      AcsCardRow(
        icon = Icons.Default.Info,
        title = "Anvil Studio",
        subtitle = "Version 1.0.0-dev • Built for Android"
      )
    }
  }
}