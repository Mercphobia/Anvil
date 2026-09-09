package dev.anvil.ade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.model.ProjectFile
import dev.anvil.ade.viewmodel.AnvilViewModel

@Composable
fun WorkingTreeDrawer(
  viewModel: AnvilViewModel,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val branch by viewModel.gitBranch.collectAsState()
  val workspaceTree by viewModel.workspaceTree.collectAsState()
  val selectedFile by viewModel.selectedFile.collectAsState()
  val activeType by viewModel.activeType.collectAsState()

  Surface(
    modifier = modifier
      .fillMaxHeight()
      .width(320.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .padding(16.dp)
    ) {
      // Header: Title + Close Button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(30.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Working Tree",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Workspace Files & Git Status",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 10.sp
            )
          }
        }

        IconButton(
          onClick = onClose,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Tutup Sidebar",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Current Branch Badge
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.CallSplit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "BRANCH",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = branch,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1
            )
          }
        }
      }

      HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        modifier = Modifier.padding(bottom = 12.dp)
      )

      // Scrollable Body: Changes & Workspace Tree
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .verticalScroll(rememberScrollState())
      ) {
        // Section: Working Changes (Git Status)
        Text(
          text = "CHANGES",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 10.sp,
          letterSpacing = 1.sp,
          modifier = Modifier.padding(bottom = 6.dp)
        )

        ChangeItem(
          path = "packages/SystemUI/res/layout/qs_panel.xml",
          status = "M",
          statusColor = Color(0xFFFFA726),
          onClick = {
            viewModel.setRoute("mockup")
            onClose()
          }
        )

        ChangeItem(
          path = "app/src/main/res/layout/activity_main.xml",
          status = "A",
          statusColor = Color(0xFF66BB6A),
          onClick = {
            val file = findFileByPath(workspaceTree, "app/src/main/res/layout/activity_main.xml")
            if (file != null) viewModel.openFileFromTree(file)
            onClose()
          }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section: File Tree
        Text(
          text = "WORKSPACE TREE",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 10.sp,
          letterSpacing = 1.sp,
          modifier = Modifier.padding(bottom = 6.dp)
        )

        workspaceTree.forEach { item ->
          FileTreeItemView(
            item = item,
            selectedFile = selectedFile,
            depth = 0,
            onFileClick = { file ->
              viewModel.openFileFromTree(file)
              onClose()
            }
          )
        }
      }

      HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        modifier = Modifier.padding(vertical = 12.dp)
      )

      // Quick Tools Shortcuts at Bottom
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        QuickToolButton(
          icon = Icons.Filled.Terminal,
          label = "Terminal",
          onClick = {
            viewModel.setRoute("terminal")
            onClose()
          },
          modifier = Modifier.weight(1f)
        )

        QuickToolButton(
          icon = Icons.AutoMirrored.Filled.CallSplit,
          label = "Git",
          onClick = {
            viewModel.setRoute("git")
            onClose()
          },
          modifier = Modifier.weight(1f)
        )

        QuickToolButton(
          icon = Icons.Filled.HelpOutline,
          label = "Slide",
          onClick = {
            viewModel.openWelcome()
            onClose()
          },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun ChangeItem(
  path: String,
  status: String,
  statusColor: Color,
  onClick: () -> Unit
) {
  val fileName = path.substringAfterLast("/")
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(8.dp),
    color = Color.Transparent,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(18.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(statusColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = status,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = statusColor
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = fileName,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          maxLines = 1
        )
        Text(
          text = path,
          fontSize = 9.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontFamily = FontFamily.Monospace,
          maxLines = 1
        )
      }
    }
  }
}

@Composable
private fun FileTreeItemView(
  item: ProjectFile,
  selectedFile: ProjectFile?,
  depth: Int,
  onFileClick: (ProjectFile) -> Unit
) {
  val isSelected = selectedFile?.path == item.path

  if (item.isDirectory) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = (depth * 12).dp, top = 4.dp, bottom = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Filled.Folder,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = item.name,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    item.children.forEach { child ->
      FileTreeItemView(
        item = child,
        selectedFile = selectedFile,
        depth = depth + 1,
        onFileClick = onFileClick
      )
    }
  } else {
    val isXml = item.name.endsWith(".xml")
    val isJava = item.name.endsWith(".java")

    Surface(
      onClick = { onFileClick(item) },
      shape = RoundedCornerShape(8.dp),
      color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent,
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = (depth * 12).dp, top = 2.dp, bottom = 2.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = if (isXml) Icons.Filled.Code else if (isJava) Icons.Filled.InsertDriveFile else Icons.Filled.InsertDriveFile,
          contentDescription = null,
          tint = if (isXml) MaterialTheme.colorScheme.secondary else if (isJava) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = item.name,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
          maxLines = 1
        )
      }
    }
  }
}

@Composable
private fun QuickToolButton(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun findFileByPath(tree: List<ProjectFile>, path: String): ProjectFile? {
  for (item in tree) {
    if (item.path == path) return item
    if (item.isDirectory) {
      val found = findFileByPath(item.children, path)
      if (found != null) return found
    }
  }
  return null
}
