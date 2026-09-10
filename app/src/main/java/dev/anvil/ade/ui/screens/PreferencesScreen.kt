package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.components.AcsCardGroup
import dev.anvil.ade.ui.components.AcsCardRow
import dev.anvil.ade.ui.components.AcsCardSwitchRow
import dev.anvil.ade.ui.components.AcsSectionLabel
import dev.anvil.ade.ui.theme.*

/** IDE Preferences: editor, keymap, font size, and auto-save settings. */
@Composable
fun PreferencesScreen(
  editorFontSize: Int,
  showLineNumbers: Boolean,
  wordWrap: Boolean,
  autoSave: Boolean,
  onEditorFontSize: (Int) -> Unit,
  onShowLineNumbers: (Boolean) -> Unit,
  onWordWrap: (Boolean) -> Unit,
  onAutoSave: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AcsBg)
      .verticalScroll(rememberScrollState())
      .padding(bottom = 32.dp)
  ) {
    AcsSectionLabel("EDITOR")

    AcsCardGroup {
      AcsCardRow(
        icon = Icons.Default.TextFields,
        title = "Font Size",
        subtitle = "${editorFontSize}sp"
      )
      AcsCardRow(
        icon = Icons.Default.FormatListNumbered,
        title = "Show Line Numbers",
        subtitle = if (showLineNumbers) "Enabled" else "Disabled"
      )
      AcsCardSwitchRow(
        icon = Icons.Default.LineStyle,
        title = "Word Wrap",
        checked = wordWrap,
        onCheckedChange = onWordWrap
      )
    }

    Spacer(Modifier.height(8.dp))

    AcsSectionLabel("WORKSPACE")

    AcsCardGroup {
      AcsCardSwitchRow(
        icon = Icons.Default.Save,
        title = "Auto Save",
        subtitle = "Save file on focus change",
        checked = autoSave,
        onCheckedChange = onAutoSave
      )
      AcsCardRow(
        icon = Icons.Default.Key,
        title = "Keymap",
        subtitle = "VSCode-compatible"
      )
      AcsCardRow(
        icon = Icons.Default.ColorLens,
        title = "Editor Theme",
        subtitle = "Dark (ACS)"
      )
    }

    Spacer(Modifier.height(8.dp))

    AcsSectionLabel("CODE COMPLETION")

    AcsCardGroup {
      AcsCardRow(
        icon = Icons.Default.Code,
        title = "AI Code Completion",
        subtitle = "Agent-powered suggestions"
      )
      AcsCardRow(
        icon = Icons.Default.Lightbulb,
        title = "Inline Hints",
        subtitle = "Parameter names and types"
      )
    }
  }
}