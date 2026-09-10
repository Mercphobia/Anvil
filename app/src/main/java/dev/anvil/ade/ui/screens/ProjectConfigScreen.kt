package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.model.ProjectType
import dev.anvil.ade.ui.components.ProviderPill
import dev.anvil.ade.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectConfigScreen(
  projectType: ProjectType,
  onBack: () -> Unit,
  onCreateProject: (
    name: String,
    packageName: String,
    minSdk: String,
    targetSdk: String,
    language: String
  ) -> Unit,
  modifier: Modifier = Modifier
) {
  var projectName by remember { mutableStateOf("") }
  var packageName by remember { mutableStateOf("com.example.myapp") }
  var minSdk by remember { mutableStateOf("24") }
  var targetSdk by remember { mutableStateOf("35") }
  var language by remember { mutableStateOf("Kotlin") }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AcsBg)
  ) {
    // Top bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(AcsSurface1)
        .padding(horizontal = 8.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.Default.ArrowBack, "Back", tint = AcsOnSurface)
      }
      Spacer(Modifier.width(8.dp))
      Text(
        "New Project",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = AcsOnSurface
      )
      Spacer(Modifier.weight(1f))
      ProviderPill(projectType = projectType)
    }

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(scrollState)
        .padding(20.dp)
    ) {
      // Project name
      ConfigField("Project Name", "MyApp") { projectName = it }

      Spacer(Modifier.height(16.dp))

      // Package name
      ConfigField(
        "Package Name",
        "com.example.myapp",
        initial = packageName
      ) { packageName = it }

      Spacer(Modifier.height(16.dp))

      // Min SDK
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
          ConfigField("Min SDK", "24", initial = minSdk) { minSdk = it }
        }
        Column(Modifier.weight(1f)) {
          ConfigField("Target SDK", "35", initial = targetSdk) { targetSdk = it }
        }
      }

      Spacer(Modifier.height(16.dp))

      // Language selector
      Text("Language", fontSize = 12.sp, color = AcsOnSurfaceVariant)
      Spacer(Modifier.height(6.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Kotlin", "Java", "Kotlin + Compose").forEach { lang ->
          FilterChip(
            selected = language == lang,
            onClick = { language = lang },
            label = { Text(lang, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AcsGold.copy(alpha = 0.15f),
              selectedLabelColor = AcsGold
            ),
            shape = RoundedCornerShape(8.dp)
          )
        }
      }
    }

    // Bottom button
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(AcsSurface1)
        .padding(16.dp)
    ) {
      Button(
        onClick = {
          onCreateProject(projectName.ifBlank { "MyApp" }, packageName, minSdk, targetSdk, language)
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AcsGold),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(
          "Create Project",
          color = AcsBg,
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}

@Composable
private fun ConfigField(
  label: String,
  placeholder: String,
  initial: String = "",
  onValueChange: (String) -> Unit
) {
  var value by remember { mutableStateOf(initial) }
  Text(label, fontSize = 12.sp, color = AcsOnSurfaceVariant)
  Spacer(Modifier.height(6.dp))
  OutlinedTextField(
    value = value,
    onValueChange = {
      value = it
      onValueChange(it)
    },
    placeholder = { Text(placeholder, color = AcsOnSurfaceDim) },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
    colors = OutlinedTextFieldDefaults.colors(
      focusedBorderColor = AcsGold,
      unfocusedBorderColor = AcsOutline,
      focusedTextColor = AcsOnSurface,
      unfocusedTextColor = AcsOnSurface,
      cursorColor = AcsGold
    ),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    shape = RoundedCornerShape(10.dp)
  )
}