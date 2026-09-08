package com.vibe.forge.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibe.forge.model.ProjectFile
import com.vibe.forge.ui.components.OpenProjectDialog
import com.vibe.forge.ui.components.ProjectTemplateDialog
import com.vibe.forge.ui.theme.ForgeTerminalBg
import com.vibe.forge.viewmodel.VibeForgeViewModel

@Composable
fun ProjectScreen(
  viewModel: VibeForgeViewModel,
  modifier: Modifier = Modifier
) {
  val workspaceTree by viewModel.workspaceTree.collectAsState()
  val selectedFile by viewModel.selectedFile.collectAsState()
  val editorContent by viewModel.editorContent.collectAsState()
  val savedNotice by viewModel.editorSavedNotice.collectAsState()
  val activeProjectName by viewModel.activeProjectName.collectAsState()
  val projectNotice by viewModel.projectNotice.collectAsState()
  val showOpenProjectDialog by viewModel.showOpenProjectDialog.collectAsState()

  var showFileExplorer by remember { mutableStateOf(false) }
  var showTemplateDialog by remember { mutableStateOf(false) }

  if (showTemplateDialog) {
    ProjectTemplateDialog(
      onDismiss = { showTemplateDialog = false },
      onSelectTemplate = { templateId ->
        viewModel.applyProjectTemplate(templateId)
      }
    )
  }

  if (showOpenProjectDialog) {
    OpenProjectDialog(
      onDismiss = { viewModel.toggleOpenProjectDialog(false) },
      onOpenLocal = { name, path ->
        viewModel.openLocalProject(name, path)
      },
      onCloneGitHub = { url, branch, token ->
        viewModel.cloneGitHubProject(url, branch, token)
      }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Project Notice Banner (if any)
    projectNotice?.let { notice ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = notice,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
          )
          TextButton(onClick = { viewModel.clearProjectNotice() }) {
            Text("Tutup", fontSize = 11.sp)
          }
        }
      }
    }

    // Header Bar with File Name, Language Badge & Action Buttons
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .weight(1f)
          .clickable { showFileExplorer = !showFileExplorer }
          .padding(vertical = 4.dp)
      ) {
        Icon(
          imageVector = if (showFileExplorer) Icons.Filled.FolderOpen else Icons.Filled.Folder,
          contentDescription = "Toggle Files",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = selectedFile?.name ?: activeProjectName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
          Text(
            text = if (showFileExplorer) "Tutup berkas" else (selectedFile?.path ?: activeProjectName),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Template Picker Button
        OutlinedButton(
          onClick = { showTemplateDialog = true },
          shape = RoundedCornerShape(10.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.testTag("open_template_button")
        ) {
          Icon(Icons.Filled.Dashboard, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Template", fontSize = 11.sp)
        }

        // Open Project (Local / GitHub) Button
        OutlinedButton(
          onClick = { viewModel.toggleOpenProjectDialog(true) },
          shape = RoundedCornerShape(10.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.testTag("open_project_button")
        ) {
          Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Buka", fontSize = 11.sp)
        }

        // Save Button
        Button(
          onClick = { viewModel.saveCurrentFile() },
          shape = RoundedCornerShape(10.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier.testTag("save_file_button")
        ) {
          Icon(Icons.Filled.Save, contentDescription = "Save", modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Simpan", fontSize = 11.sp)
        }
      }
    }

    // Expandable File Explorer Pane
    AnimatedVisibility(
      visible = showFileExplorer,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Struktur Workspace Proyek",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          // Recursive/Flat file items
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            workspaceTree.forEach { root ->
              item {
                FileTreeItem(
                  file = root,
                  level = 0,
                  selectedFile = selectedFile,
                  onSelect = {
                    viewModel.selectFile(it)
                    showFileExplorer = false
                  }
                )
              }
              root.children.forEach { child ->
                item {
                  FileTreeItem(
                    file = child,
                    level = 1,
                    selectedFile = selectedFile,
                    onSelect = {
                      viewModel.selectFile(it)
                      showFileExplorer = false
                    }
                  )
                }
                child.children.forEach { subChild ->
                  item {
                    FileTreeItem(
                      file = subChild,
                      level = 2,
                      selectedFile = selectedFile,
                      onSelect = {
                        viewModel.selectFile(it)
                        showFileExplorer = false
                      }
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Code Editor Container
    ElevatedCard(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
      )
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Editor sub-header with lines and formatting
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 14.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${selectedFile?.path ?: "file"} • ${editorContent.lines().size} lines",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "UTF-8 • Java/XML Engine",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary
          )
        }

        // Code Editor Text Area with line numbers simulation
        Row(modifier = Modifier.fillMaxSize()) {
          // Line numbers gutter
          val lineCount = editorContent.lines().size.coerceAtLeast(1)
          Column(
            modifier = Modifier
              .background(MaterialTheme.colorScheme.surfaceContainerLow)
              .fillMaxHeight()
              .padding(horizontal = 8.dp, vertical = 12.dp)
          ) {
            for (i in 1..lineCount.coerceAtMost(50)) {
              Text(
                text = "$i",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                lineHeight = 20.sp
              )
            }
          }

          // Editable code
          OutlinedTextField(
            value = editorContent,
            onValueChange = { viewModel.updateEditorContent(it) },
            modifier = Modifier
              .fillMaxSize()
              .testTag("code_editor_field"),
            textStyle = TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              lineHeight = 20.sp,
              color = MaterialTheme.colorScheme.onSurface
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Color.Transparent,
              unfocusedBorderColor = Color.Transparent,
              focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
          )
        }
      }
    }
  }
}

@Composable
private fun FileTreeItem(
  file: ProjectFile,
  level: Int,
  selectedFile: ProjectFile?,
  onSelect: (ProjectFile) -> Unit
) {
  val isSelected = selectedFile?.path == file.path
  Surface(
    onClick = { onSelect(file) },
    shape = RoundedCornerShape(8.dp),
    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = (level * 16).dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = if (file.isDirectory) Icons.Filled.Folder else Icons.Filled.Description,
        contentDescription = null,
        tint = if (file.isDirectory) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = file.name,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
