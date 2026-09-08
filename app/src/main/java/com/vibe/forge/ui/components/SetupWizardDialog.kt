package com.vibe.forge.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vibe.forge.model.AgentMode

@Composable
fun SetupWizardDialog(
  onDismiss: () -> Unit,
  onComplete: (provider: String, apiKey: String, mode: AgentMode, template: String, repoUrl: String) -> Unit
) {
  var currentStep by remember { mutableIntStateOf(1) }
  var selectedProvider by remember { mutableStateOf("Google Gemini") }
  var apiKeyInput by remember { mutableStateOf("") }
  var selectedMode by remember { mutableStateOf(AgentMode.MODE_A) }
  var selectedTemplate by remember { mutableStateOf("empty_activity") }
  var githubRepoUrl by remember { mutableStateOf("") }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 24.dp)
        .testTag("setup_wizard_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.RocketLaunch,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Setup Wizard VibeForge",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.3).sp
            )
            Text(
              text = "Langkah $currentStep dari 3 • Konfigurasi Lingkungan",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step Progress Indicator
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          for (i in 1..3) {
            Box(
              modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(CircleShape)
                .background(
                  if (i <= currentStep) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step Content with Animation
        AnimatedContent(
          targetState = currentStep,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "WizardStepTransition"
        ) { step ->
          when (step) {
            1 -> Step1ProviderConfig(
              selectedProvider = selectedProvider,
              onSelectProvider = { selectedProvider = it },
              apiKey = apiKeyInput,
              onApiKeyChange = { apiKeyInput = it }
            )
            2 -> Step2ModeAndToolchain(
              selectedMode = selectedMode,
              onSelectMode = { selectedMode = it }
            )
            3 -> Step3ProjectTemplate(
              selectedTemplate = selectedTemplate,
              onSelectTemplate = { selectedTemplate = it },
              repoUrl = githubRepoUrl,
              onRepoUrlChange = { githubRepoUrl = it }
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("wizard_skip_button")
          ) {
            Text("Lewati Setup", color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (currentStep > 1) {
              OutlinedButton(
                onClick = { currentStep-- },
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("Kembali")
              }
            }

            Button(
              onClick = {
                if (currentStep < 3) {
                  currentStep++
                } else {
                  onComplete(selectedProvider, apiKeyInput, selectedMode, selectedTemplate, githubRepoUrl)
                }
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("wizard_next_button")
            ) {
              Text(if (currentStep == 3) "Mulai Studio" else "Lanjut")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun Step1ProviderConfig(
  selectedProvider: String,
  onSelectProvider: (String) -> Unit,
  apiKey: String,
  onApiKeyChange: (String) -> Unit
) {
  val providers = listOf(
    "Google Gemini" to "Gemini 2.5 Flash • Server & Client AI",
    "OpenAI" to "GPT-4o • Direct API",
    "Anthropic Claude" to "Claude 3.5 Sonnet",
    "DeepSeek" to "DeepSeek-V3 Coder",
    "Local Ollama" to "Ollama Android Local Daemon"
  )

  Column {
    Text(
      text = "Pilih LLM Provider untuk Asisten AI",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold
    )
    Text(
      text = "Kecerdasan AI akan memandu penulisan kode, perbaikan error, dan desain UI.",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    providers.forEach { (name, desc) ->
      val isSelected = selectedProvider == name
      Card(
        onClick = { onSelectProvider(name) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
          else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .then(
            if (isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
            else Modifier
          )
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    OutlinedTextField(
      value = apiKey,
      onValueChange = onApiKeyChange,
      label = { Text("API Key (Opsional / Default)") },
      placeholder = { Text("Kosongkan untuk memakai server AI Studio") },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth(),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary
      )
    )
  }
}

@Composable
private fun Step2ModeAndToolchain(
  selectedMode: AgentMode,
  onSelectMode: (AgentMode) -> Unit
) {
  Column {
    Text(
      text = "Pilih Fokus Kerja Studio",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold
    )
    Text(
      text = "Pilih orientasi proyek Anda. Anda dapat berpindah kapan saja dari bilah atas.",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    // Mode A Card
    val isModeA = selectedMode == AgentMode.MODE_A
    Card(
      onClick = { onSelectMode(AgentMode.MODE_A) },
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (isModeA) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surfaceContainerLow
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .then(
          if (isModeA) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
          else Modifier
        )
    ) {
      Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.Android,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text("Mode A: App Builder (APK)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text("Kompilasi on-device via aapt2, ecj, d8, dan instalasi APK mandiri.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    // Mode B Card
    val isModeB = selectedMode == AgentMode.MODE_B
    Card(
      onClick = { onSelectMode(AgentMode.MODE_B) },
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (isModeB) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surfaceContainerLow
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .then(
          if (isModeB) Modifier.border(1.5.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(14.dp))
          else Modifier
        )
    ) {
      Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.Palette,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text("Mode B: AOSP SystemUI Assist", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text("Fokus desain Quick Settings, Status Bar, dan XML overlay dengan token Monet.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Toolchain Health Check Card
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        Text("Status Toolchain On-Device:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          ToolStatusBadge("aapt2", true)
          ToolStatusBadge("ecj", true)
          ToolStatusBadge("d8", true)
          ToolStatusBadge("apksigner", true)
        }
      }
    }
  }
}

@Composable
private fun ToolStatusBadge(name: String, ready: Boolean) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(if (ready) Color(0xFF4CAF50) else Color(0xFFFF5722))
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(name, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
  }
}

@Composable
private fun Step3ProjectTemplate(
  selectedTemplate: String,
  onSelectTemplate: (String) -> Unit,
  repoUrl: String,
  onRepoUrlChange: (String) -> Unit
) {
  val templates = listOf(
    Triple("empty_activity", "Empty Activity", "Activity bersih dengan FrameLayout kosong tanpa bloatware."),
    Triple("no_activity", "No Activity (Service)", "Modul latar belakang murni tanpa antarmuka GUI (Headless daemon)."),
    Triple("basic_views", "Basic Views Activity", "Activity interaktif siap pakai dengan TextView & Button kalkulator."),
    Triple("aosp_overlay", "AOSP SystemUI Overlay", "Overlay XML QuickSettings dengan token dynamic Monet."),
    Triple("github_clone", "Clone dari GitHub", "Kloning repositori Git publik/privat ke workspace lokal.")
  )

  Column {
    Text(
      text = "Pilih Template Proyek Awal",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold
    )
    Text(
      text = "VibeForge akan menyusun struktur file otomatis untuk memulai.",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(bottom = 12.dp)
    )

    templates.forEach { (id, title, desc) ->
      val isSelected = selectedTemplate == id
      Card(
        onClick = { onSelectTemplate(id) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
          else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .then(
            if (isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
            else Modifier
          )
      ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = when (id) {
              "empty_activity" -> Icons.Filled.Layers
              "no_activity" -> Icons.Filled.Memory
              "basic_views" -> Icons.Filled.Code
              "github_clone" -> Icons.Filled.CloudDownload
              else -> Icons.Filled.Palette
            },
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          if (isSelected) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    if (selectedTemplate == "github_clone") {
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = repoUrl,
        onValueChange = onRepoUrlChange,
        label = { Text("URL Repositori GitHub") },
        placeholder = { Text("https://github.com/user/repo.git") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary
        )
      )
    }
  }
}
