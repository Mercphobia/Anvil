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
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenProjectDialog(
  onDismiss: () -> Unit,
  onOpenLocal: (name: String, path: String) -> Unit,
  onCloneGitHub: (url: String, branch: String, token: String) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }

  // Local tab state
  var projectName by remember { mutableStateOf("MyAndroidApp") }
  var projectPath by remember { mutableStateOf("/sdcard/VibeForge/MyAndroidApp") }

  // GitHub tab state
  var repoUrl by remember { mutableStateOf("https://github.com/Mercphobia/VibeForge.git") }
  var branchName by remember { mutableStateOf("main") }
  var authToken by remember { mutableStateOf("") }

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
        .testTag("open_project_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Dialog Title
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.FolderOpen,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Buka Proyek",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Buka dari penyimpanan lokal atau klon dari GitHub",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        PrimaryTabRow(
          selectedTabIndex = selectedTab,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Lokal (Storage)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
            icon = { Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("GitHub Clone", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
            icon = { Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp)) }
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
          // Local Storage Tab
          Text("Konfigurasi Proyek Lokal", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = projectName,
            onValueChange = { projectName = it },
            label = { Text("Nama Proyek") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = projectPath,
            onValueChange = { projectPath = it },
            label = { Text("Path Direktori Lokal") },
            placeholder = { Text("/sdcard/VibeForge/...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))
          Text("Pilihan Direktori Cepat:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            SuggestionChip(
              onClick = {
                projectName = "CalculatorSample"
                projectPath = "/sdcard/VibeForge/CalculatorSample"
              },
              label = { Text("Calculator", fontSize = 11.sp) }
            )
            SuggestionChip(
              onClick = {
                projectName = "MonetSystemUI"
                projectPath = "/sdcard/AOSP/packages/SystemUI"
              },
              label = { Text("SystemUI", fontSize = 11.sp) }
            )
          }
        } else {
          // GitHub Tab
          Text("Klon Repository dari GitHub", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = repoUrl,
            onValueChange = { repoUrl = it },
            label = { Text("URL Repository GitHub") },
            placeholder = { Text("https://github.com/user/repo.git") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = branchName,
              onValueChange = { branchName = it },
              label = { Text("Branch") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
              value = authToken,
              onValueChange = { authToken = it },
              label = { Text("Token (Opsional)") },
              placeholder = { Text("ghp_...") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1.3f)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text("Preset Repository:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            SuggestionChip(
              onClick = {
                repoUrl = "https://github.com/Mercphobia/VibeForge.git"
                branchName = "main"
              },
              label = { Text("Mercphobia/VibeForge", fontSize = 11.sp) }
            )
            SuggestionChip(
              onClick = {
                repoUrl = "https://github.com/android/nowinandroid.git"
                branchName = "main"
              },
              label = { Text("NowInAndroid", fontSize = 11.sp) }
            )
          }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Batal")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (selectedTab == 0) {
                onOpenLocal(projectName, projectPath)
              } else {
                onCloneGitHub(repoUrl, branchName, authToken)
              }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("submit_open_project")
          ) {
            Text(if (selectedTab == 0) "Buka Folder" else "Clone & Buka")
          }
        }
      }
    }
  }
}
