package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*
import dev.anvil.ade.ui.components.AcsCardGroup
import dev.anvil.ade.ui.components.AcsCardRow
import dev.anvil.ade.ui.components.AcsCardSwitchRow
import dev.anvil.ade.ui.components.AcsSectionLabel

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
    AcsCardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
      AcsCardSwitchRow(
        icon = Icons.Filled.Build,
        title = "Automatic installation",
        checked = autoInstall,
        onCheckedChange = { autoInstall = it }
      )
    }

    Spacer(Modifier.height(16.dp))

    // Dropdown selections
    AcsSectionLabel("SDK Versions", modifier = Modifier.padding(horizontal = 16.dp))
    AcsCardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
      ConfigDropdown("Android SDK version", sdkVersion, sdkVersions, showSdkDropdown, { showSdkDropdown = !showSdkDropdown }, { sdkVersion = it; showSdkDropdown = false })
      ConfigDropdown("JDK version", jdkVersion, jdkVersions, showJdkDropdown, { showJdkDropdown = !showJdkDropdown }, { jdkVersion = it; showJdkDropdown = false })
      ConfigDropdown("Android NDK version", ndkVersion, ndkVersions, showNdkDropdown, { showNdkDropdown = !showNdkDropdown }, { ndkVersion = it; showNdkDropdown = false })
    }

    Spacer(Modifier.height(12.dp))

    // Toggle options
    AcsSectionLabel("Additional Tools", modifier = Modifier.padding(horizontal = 16.dp))
    AcsCardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
      AcsCardSwitchRow(Icons.Filled.Code, "Install Git", checked = installGit, onCheckedChange = { installGit = it })
      AcsCardSwitchRow(Icons.Filled.Key, "Install OpenSSH", checked = installSsh, onCheckedChange = { installSsh = it })
    }

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
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)) {
    Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AcsGold, letterSpacing = 0.5.sp)
    Spacer(Modifier.height(6.dp))
    Box {
      Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerHigh)
          .clickable(onClick = onToggle)
          .padding(horizontal = 14.dp, vertical = 11.dp),
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