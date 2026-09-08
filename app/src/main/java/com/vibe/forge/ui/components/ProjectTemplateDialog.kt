package com.vibe.forge.ui.components

import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ProjectTemplateDialog(
  onDismiss: () -> Unit,
  onSelectTemplate: (templateId: String) -> Unit
) {
  var selectedId by remember { mutableStateOf("empty_activity") }

  val templates = listOf(
    Triple("empty_activity", "Empty Activity", "Activity dasar minimal dengan layout XML FrameLayout siap modifikasi tanpa ketergantungan luar."),
    Triple("no_activity", "No Activity (Background Service)", "Modul headless tanpa antarmuka GUI, cocok untuk background service atau library Android."),
    Triple("basic_views", "Basic Views Activity", "Template kalkulator & counter interaktif lengkap dengan penanganan event tombol dan TextView."),
    Triple("aosp_overlay", "AOSP SystemUI Overlay", "Overlay XML Quick Settings panel dengan token Monet dynamic Material Design 3."),
    Triple("github_clone", "Clone dari GitHub", "Kloning repositori Git publik/privat langsung ke workspace lokal.")
  )

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
        .testTag("project_template_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.Dashboard,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Template Proyek Android",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Pilih scaffold struktur proyek yang ingin dibuat",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        templates.forEach { (id, title, desc) ->
          val isSelected = selectedId == id
          Card(
            onClick = { selectedId = id },
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
                imageVector = when (id) {
                  "empty_activity" -> Icons.Filled.Layers
                  "no_activity" -> Icons.Filled.Memory
                  "basic_views" -> Icons.Filled.Code
                  "github_clone" -> Icons.Filled.CloudDownload
                  else -> Icons.Filled.Palette
                },
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Batal")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              onSelectTemplate(selectedId)
              onDismiss()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("apply_template_button")
          ) {
            Text("Terapkan Template")
          }
        }
      }
    }
  }
}
