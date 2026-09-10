package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.editor.SoraEditorWrapper
import dev.anvil.ade.ui.components.ErrorChipPeek
import dev.anvil.ade.ui.components.ErrorLogBottomSheet
import dev.anvil.ade.viewmodel.AnvilViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import dev.anvil.ade.ui.theme.GeistMono

/**
 * Code editor screen (anvil_ui spec Bagian 1). Opened when a file is tapped
 * in the file tree / Project Sidebar. Owns the top bar (file name + relative
 * path), the editor area, and — when the last build had errors — the
 * [ErrorChipPeek] strip and the expandable [ErrorLogBottomSheet].
 */
@Composable
fun EditorScreen(
  viewModel: AnvilViewModel,
  onOpenSidebar: () -> Unit,
  modifier: Modifier = Modifier
) {
  val selectedFile by viewModel.selectedFile.collectAsState()
  val editorContent by viewModel.editorContent.collectAsState()
  val buildErrors by viewModel.buildErrors.collectAsState()

  var errorSheetOpen by remember { mutableStateOf(false) }
  var scrollRequestLine by remember { mutableStateOf<Int?>(null) }

  Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
    // ---- Top bar: file tabs (120dp, scrollable, close button) ----
    val openFiles = remember { mutableStateListOf<dev.anvil.ade.model.ProjectFile>() }
    LaunchedEffect(selectedFile) {
      selectedFile?.let { file ->
        if (openFiles.none { it.path == file.path }) {
          openFiles.add(file)
        }
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onOpenSidebar,
        modifier = Modifier.size(18.dp)
      ) {
        Icon(
          imageVector = Icons.Filled.Menu,
          contentDescription = "Buka Project Sidebar",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.width(8.dp))

      LazyRow(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        items(openFiles.toList(), key = { it.path }) { file ->
          val isSelected = file.path == (selectedFile?.path ?: "")
          Box(
            modifier = Modifier
              .width(120.dp)
              .height(32.dp)
              .background(
                color = if (isSelected)
                  MaterialTheme.colorScheme.surfaceContainer
                else
                  MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
              )
              .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Row(
              modifier = Modifier.fillMaxSize(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = file.name,
                fontSize = 11.sp,
                fontFamily = GeistMono,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
              )
              IconButton(
                onClick = {
                  openFiles.remove(file)
                  if (selectedFile?.path == file.path) {
                    openFiles.lastOrNull()?.let { viewModel.selectFile(it) }
                  }
                },
                modifier = Modifier.size(16.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Tutup tab",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(10.dp)
                )
              }
            }
          }
        }
      }

      if (selectedFile?.name?.endsWith(".xml") == true) {
        IconButton(onClick = { viewModel.setRoute("mockup") }) {
          Icon(
            imageVector = Icons.Filled.Preview,
            contentDescription = "Preview layout",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }
      }
      IconButton(
        onClick = { viewModel.saveCurrentFile() },
        modifier = Modifier.size(20.dp)
      ) {
        Icon(
          imageVector = Icons.Filled.Save,
          contentDescription = "Simpan file",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
    HorizontalDivider(
      thickness = 0.5.dp,
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )

    // ---- Editor area (remaining height minus error chip when present) ----
    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
      if (selectedFile != null) {
        SoraEditorWrapper(
          text = editorContent,
          onTextChanged = { viewModel.updateEditorContent(it) },
          fileName = selectedFile?.name,
          modifier = Modifier.fillMaxSize(),
          scrollRequestLine = scrollRequestLine,
          onScrollHandled = { scrollRequestLine = null }
        )
      } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            text = "Open the left drawer for files",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // ---- Error log bottom sheet (peek: first row partially visible) ----
      if (buildErrors.isNotEmpty() && errorSheetOpen) {
        ErrorLogBottomSheet(
          errors = buildErrors,
          onRetry = { viewModel.runBuildPipeline() },
          onDismiss = { errorSheetOpen = false },
          onErrorClick = { err ->
            errorSheetOpen = false
            scrollRequestLine = err.line
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .align(Alignment.BottomCenter)
        )
      }
    }

    // ---- Error chip peek (only rendered when errors exist) ----
    if (buildErrors.isNotEmpty() && !errorSheetOpen) {
      ErrorChipPeek(
        errorCount = buildErrors.size,
        onClick = { errorSheetOpen = true }
      )
    }
  }
}
