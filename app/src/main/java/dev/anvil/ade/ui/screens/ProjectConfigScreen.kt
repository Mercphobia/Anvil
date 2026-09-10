package dev.anvil.ade.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.AcsGold
import dev.anvil.ade.ui.theme.AcsSurface4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectConfigScreen(
  onBack: () -> Unit,
  onCreateProject: (name: String, packageName: String, location: String, language: String, minSdk: String, useKts: Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var projectName by remember { mutableStateOf("MyApp") }
  var packageName by remember { mutableStateOf("com.example.myapp") }
  var projectLocation by remember { mutableStateOf("/storage/emulated/0/AndroidIDEProjects") }
  var selectedLanguage by remember { mutableStateOf("Java") }
  var selectedMinSdk by remember { mutableStateOf("API 21: Android 5.0 (Lollipop)") }
  var useKts by remember { mutableStateOf(true) }
  var showLanguageDropdown by remember { mutableStateOf(false) }
  var showSdkDropdown by remember { mutableStateOf(false) }

  val languages = listOf("Java", "Kotlin")
  val sdks = listOf(
    "API 21: Android 5.0 (Lollipop)",
    "API 24: Android 7.0 (Nougat)",
    "API 26: Android 8.0 (Oreo)",
    "API 28: Android 9.0 (Pie)",
    "API 31: Android 12",
    "API 34: Android 14"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
  ) {
    // Top bar
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(
          Icons.AutoMirrored.Filled.ArrowBack, "Back",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Text(
        "Project Configuration",
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    Spacer(Modifier.height(8.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

    // Form fields
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
      ConfigField("Project name", projectName) { projectName = it }
      ConfigField("Package name", packageName) { packageName = it }
      ConfigField("Project location", projectLocation, trailing = {
        IconButton(onClick = {}, modifier = Modifier.size(20.dp)) {
          Icon(Icons.Filled.FolderOpen, "Browse", tint = AcsGold, modifier = Modifier.size(16.dp))
        }
      }) { projectLocation = it }

      Spacer(Modifier.height(12.dp))

      // Language dropdown
      ConfigDropdown(
        label = "Language",
        value = selectedLanguage,
        options = languages,
        expanded = showLanguageDropdown,
        onToggle = { showLanguageDropdown = !showLanguageDropdown },
        onSelect = { selectedLanguage = it; showLanguageDropdown = false }
      )

      Spacer(Modifier.height(12.dp))

      // Min SDK dropdown
      ConfigDropdown(
        label = "Minimum SDK",
        value = selectedMinSdk,
        options = sdks,
        expanded = showSdkDropdown,
        onToggle = { showSdkDropdown = !showSdkDropdown },
        onSelect = { selectedMinSdk = it; showSdkDropdown = false }
      )

      Spacer(Modifier.height(12.dp))

      // Kotlin DSL toggle
      Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceContainer)
          .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Use Gradle Kotlin DSL (.kts)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
          Text("Modern Gradle configuration using Kotlin", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = useKts, onCheckedChange = { useKts = it }, colors = SwitchDefaults.colors(checkedTrackColor = AcsGold))
      }
    }

    Spacer(Modifier.weight(1f))

    // Create button
    Button(
      onClick = { onCreateProject(projectName, packageName, projectLocation, selectedLanguage, selectedMinSdk, useKts) },
      modifier = Modifier.fillMaxWidth().padding(20.dp).height(50.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = AcsGold)
    ) {
      Text("Create Project", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF0D0B0A))
    }
  }
}

@Composable
private fun ConfigField(label: String, value: String, trailing: @Composable (() -> Unit)? = null, onValueChange: (String) -> Unit) {
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
    Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
      value = value, onValueChange = onValueChange,
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AcsGold, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
      ),
      shape = RoundedCornerShape(10.dp),
      trailingIcon = trailing
    )
  }
}

@Composable
private fun ConfigDropdown(label: String, value: String, options: List<String>, expanded: Boolean, onToggle: () -> Unit, onSelect: (String) -> Unit) {
  Column {
    Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
    Spacer(Modifier.height(6.dp))
    Box {
      Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceContainer).clickable(onClick = onToggle)
          .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text("▾", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      DropdownMenu(expanded = expanded, onDismissRequest = { onSelect(value) }) {
        options.forEach { opt ->
          DropdownMenuItem(
            text = { Text(opt) },
            onClick = { onSelect(opt) },
            trailingIcon = if (opt == value) {{ Icon(Icons.Filled.Check, null, tint = AcsGold, modifier = Modifier.size(16.dp)) }} else null
          )
        }
      }
    }
  }
}