package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun IdeConfigScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var ndkVersion by remember { mutableStateOf("Not installed") }
  var cmakeVersion by remember { mutableStateOf("Not installed") }
  var showNdkDropdown by remember { mutableStateOf(false) }
  var showCmakeDropdown by remember { mutableStateOf(false) }

  val ndkVersions = listOf("NDK 28.2.13676358", "NDK 27.0.12077973", "NDK 26.1.10909125")
  val cmakeVersions = listOf("CMake 3.28.1", "CMake 3.22.1")

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
      Text("IDE Configurations", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

    Spacer(Modifier.height(8.dp))
    Text("IDE build system configurations", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp))

    Spacer(Modifier.height(16.dp))

    AcsSectionLabel("Build Tools", modifier = Modifier.padding(horizontal = 16.dp))
    AcsCardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
      // NDK
      ConfigRow(
        label = "Android NDK",
        current = ndkVersion,
        versions = ndkVersions,
        expanded = showNdkDropdown,
        onToggle = { showNdkDropdown = !showNdkDropdown },
        onSelect = { ndkVersion = it; showNdkDropdown = false }
      )
      // CMake
      ConfigRow(
        label = "CMake",
        current = cmakeVersion,
        versions = cmakeVersions,
        expanded = showCmakeDropdown,
        onToggle = { showCmakeDropdown = !showCmakeDropdown },
        onSelect = { cmakeVersion = it; showCmakeDropdown = false }
      )
    }
  }
}

@Composable
private fun ConfigRow(
  label: String, current: String, versions: List<String>,
  expanded: Boolean, onToggle: () -> Unit, onSelect: (String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 13.dp)
  ) {
    Text(
      label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
      color = AcsGold, letterSpacing = 0.5.sp
    )
    Spacer(Modifier.height(4.dp))
    Text(current, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    Spacer(Modifier.height(8.dp))
    Box {
      Button(
        onClick = onToggle,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AcsGold.copy(alpha = 0.15f)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Icon(Icons.Filled.Download, null, tint = AcsGold, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text("Download", fontSize = 12.sp, color = AcsGold, fontWeight = FontWeight.Medium)
      }
      DropdownMenu(expanded = expanded, onDismissRequest = {}) {
        versions.forEach { v ->
          DropdownMenuItem(text = { Text(v, fontSize = 13.sp) }, onClick = { onSelect(v) })
        }
      }
    }
  }
}