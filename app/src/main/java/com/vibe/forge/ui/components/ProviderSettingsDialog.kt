package com.vibe.forge.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibe.forge.model.LlmProvider
import com.vibe.forge.model.ProviderConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsDialog(
  initial: ProviderConfig,
  onSave: (ProviderConfig) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedProvider by remember { mutableStateOf(initial.provider) }
  var apiKey by remember { mutableStateOf(initial.apiKey) }
  var model by remember { mutableStateOf(initial.model) }
  var endpoint by remember { mutableStateOf(initial.endpoint) }
  var temperature by remember { mutableFloatStateOf(initial.temperature) }
  var showApiKey by remember { mutableStateOf(false) }
  var expandedProviderMenu by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.Key,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Konfigurasi LLM Engine",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Pilih LLM provider pilihan Anda. Kunci API disimpan terenkripsi di EncryptedSharedPreferences (Android Keystore).",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Provider Dropdown
        ExposedDropdownMenuBox(
          expanded = expandedProviderMenu,
          onExpandedChange = { expandedProviderMenu = !expandedProviderMenu }
        ) {
          OutlinedTextField(
            value = selectedProvider.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Provider AI") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProviderMenu) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
          )
          ExposedDropdownMenu(
            expanded = expandedProviderMenu,
            onDismissRequest = { expandedProviderMenu = false }
          ) {
            LlmProvider.values().forEach { provider ->
              DropdownMenuItem(
                text = { Text(provider.displayName) },
                onClick = {
                  selectedProvider = provider
                  model = provider.defaultModel
                  endpoint = provider.defaultEndpoint
                  expandedProviderMenu = false
                }
              )
            }
          }
        }

        // Model TextField
        OutlinedTextField(
          value = model,
          onValueChange = { model = it },
          label = { Text("Model Identifier") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          singleLine = true
        )

        // API Key TextField
        OutlinedTextField(
          value = apiKey,
          onValueChange = { apiKey = it },
          label = { Text("API Key") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { showApiKey = !showApiKey }) {
              Icon(
                imageVector = if (showApiKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = if (showApiKey) "Sembunyikan API key" else "Tampilkan API key"
              )
            }
          },
          singleLine = true
        )

        // Custom Endpoint if needed
        if (selectedProvider == LlmProvider.CUSTOM || selectedProvider == LlmProvider.OPENROUTER) {
          OutlinedTextField(
            value = endpoint,
            onValueChange = { endpoint = it },
            label = { Text("Base URL Endpoint") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
          )
        }

        // Temperature Slider
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Temperature (Kreativitas)",
              style = MaterialTheme.typography.labelMedium
            )
            Text(
              text = String.format("%.2f", temperature),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
          Slider(
            value = temperature,
            onValueChange = { temperature = it },
            valueRange = 0f..1f,
            steps = 10
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            ProviderConfig(
              provider = selectedProvider,
              apiKey = apiKey.trim(),
              model = model.trim(),
              endpoint = endpoint.trim(),
              temperature = temperature
            )
          )
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("save_provider_button")
      ) {
        Text("Simpan Konfigurasi")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal")
      }
    },
    shape = RoundedCornerShape(28.dp)
  )
}
