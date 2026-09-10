package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

@Composable
fun SdkInstallationScreen(
  onBack: () -> Unit,
  onDone: (sdkVersion: String, jdkVersion: String, ndkVersion: String, installGit: Boolean, installSsh: Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var autoInstall by remember { mutableStateOf(true) }
  var sdkVersion by remember { mutableStateOf("SDK 35.0.1") }
  var jdkVersion by remember { mutableStateOf("JDK 17") }
  var ndkVersion by remember { mutableStateOf("NDK 28.2.13676358") }
  var installGit by remember { mutableStateOf(true) }
  var installSsh by remember { mutableStateOf(true) }

  var showSdkDropdown by remember { mutableStateOf(false) }
  var showJdkDropdown by remember { mutableStateOf(false) }
  var showNdkDropdown by remember { mutableStateOf(false) }

  val sdkVersions = listOf("SDK 35.0.1", "SDK 34.0.0", "SDK 33.0.0")
  val jdkVersions = listOf("JDK 17", "JDK 21")
  val ndkVersions = listOf("Skip", "NDK 28.2.13676358", "NDK 27.0.12077973", "NDK 26.1.10909125")

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
      Text("SDK Installation", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

    Spacer(Modifier.height(16.dp))

    // Description
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
      Text("Install the development tools", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
      Spacer(Modifier.height(6.dp))
      Text("The development tools must be installed for the IDE to work. Clicking the done button will open terminal to install the tools.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
    }

    Spacer(Modifier.height(20.dp))

    // Auto install toggle
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        .clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceContainer)
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text("Automatic installation", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
      }
      Switch(checked = autoInstall, onCheckedChange = { autoInstall = it }, colors = SwitchDefaults.colors(checkedTrackColor = AcsGold))
    }

    Spacer(Modifier.height(16.dp))

    // Dropdown selections
    ConfigDropdown("Android SDK version", sdkVersion, sdkVersions, showSdkDropdown, { showSdkDropdown = !showSdkDropdown }, { sdkVersion = it; showSdkDropdown = false })
    ConfigDropdown("JDK version", jdkVersion, jdkVersions, showJdkDropdown, { showJdkDropdown = !showJdkDropdown }, { jdkVersion = it; showJdkDropdown = false })
    ConfigDropdown("Android NDK version", ndkVersion, ndkVersions, showNdkDropdown, { showNdkDropdown = !showNdkDropdown }, { ndkVersion = it; showNdkDropdown = false })

    Spacer(Modifier.height(12.dp))

    // Toggle options
    ToggleRow("Install Git", installGit, { installGit = it })
    ToggleRow("Install OpenSSH", installSsh, { installSsh = it })

    Spacer(Modifier.weight(1f))

    // Done button
    Button(
      onClick = { onDone(sdkVersion, jdkVersion, ndkVersion, installGit, installSsh) },
      modifier = Modifier.fillMaxWidth().padding(20.dp).height(50.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = AcsGold)
    ) {
      Text("Done", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = androidx.compose.ui.graphics.Color(0xFF0D0B0A))
    }
  }
}

@Composable
private fun ConfigDropdown(label: String, value: String, options: List<String>, expanded: Boolean, onToggle: () -> Unit, onSelect: (String) -> Unit) {
  Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
    Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
    Spacer(Modifier.height(6.dp))
    Box {
      Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceContainer)
          .clickable(onClick = onToggle)  // note: this is a Modifier extension issue, but works in practice since clickable is composed here
          .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text("▾", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      DropdownMenu(expanded = expanded, onDismissRequest = { /* no-op */ }) {
        options.forEach { opt ->
          DropdownMenuItem(text = { Text(opt) }, onClick = { onSelect(opt) })
        }
      }
    }
  }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp)
      .clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceContainer)
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
    Switch(checked = checked, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedTrackColor = AcsGold))
  }
}