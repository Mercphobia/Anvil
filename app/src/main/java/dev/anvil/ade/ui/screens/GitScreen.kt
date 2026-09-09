package dev.anvil.ade.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.model.DiffLine
import dev.anvil.ade.ui.theme.ForgeDiffAddBg
import dev.anvil.ade.ui.theme.ForgeDiffAddText
import dev.anvil.ade.ui.theme.ForgeDiffDelBg
import dev.anvil.ade.ui.theme.ForgeDiffDelText
import dev.anvil.ade.ui.theme.ForgeTerminalBg
import dev.anvil.ade.viewmodel.AnvilViewModel

@Composable
fun GitScreen(
  viewModel: AnvilViewModel,
  modifier: Modifier = Modifier
) {
  val branch by viewModel.gitBranch.collectAsState()
  val remote by viewModel.gitRemote.collectAsState()
  val commitMsg by viewModel.commitMessage.collectAsState()
  val diffLines by viewModel.diffLines.collectAsState()

  LaunchedEffect(Unit) { viewModel.refreshGitDiff() }
  val pushStatus by viewModel.gitPushStatus.collectAsState()
  val isGitBusy by viewModel.isGitBusy.collectAsState()

  var showConfig by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Minimalist Branch Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.CallSplit,
          contentDescription = "Branch",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = branch,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "• safe branch",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(
        onClick = { showConfig = !showConfig },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Filled.Settings,
          contentDescription = "Git Config",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    AnimatedVisibility(visible = showConfig) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp)
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Text(
            text = "Remote: $remote",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Credential: Keystore Encrypted",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.tertiary
          )
        }
      }
    }

    // Sleek Commit & Push Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = commitMsg,
        onValueChange = { viewModel.setCommitMessage(it) },
        placeholder = { Text("Pesan commit...", fontSize = 13.sp) },
        modifier = Modifier
          .weight(1f)
          .testTag("commit_message_field"),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      Spacer(modifier = Modifier.width(8.dp))

      Button(
        onClick = { viewModel.commitAndPush() },
        modifier = Modifier
          .height(52.dp)
          .testTag("commit_push_button"),
        shape = RoundedCornerShape(12.dp),
        enabled = !isGitBusy && commitMsg.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        if (isGitBusy) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
          )
        } else {
          Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Push", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }

    // Push feedback status
    pushStatus?.let { status ->
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Diff Viewer Card
    ElevatedCard(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .testTag("git_diff_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
      )
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Unified Git Diff (${diffLines.size} lines)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
val added = diffLines.count { it.type == DiffLine.Type.ADD }
          val deleted = diffLines.count { it.type == DiffLine.Type.DELETE }
                    Text(
            text = "+$added -$deleted additions/deletions",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = ForgeDiffAddText
          )
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
          items(diffLines) { line ->
            DiffRow(line)
          }
        }
      }
    }
  }
}

@Composable
private fun DiffRow(line: DiffLine) {
  val bgColor = when (line.type) {
    DiffLine.Type.ADD -> ForgeDiffAddBg
    DiffLine.Type.DELETE -> ForgeDiffDelBg
    DiffLine.Type.HEADER -> MaterialTheme.colorScheme.surfaceContainerLow
    DiffLine.Type.CONTEXT -> Color.Transparent
  }

  val textColor = when (line.type) {
    DiffLine.Type.ADD -> ForgeDiffAddText
    DiffLine.Type.DELETE -> ForgeDiffDelText
    DiffLine.Type.HEADER -> MaterialTheme.colorScheme.primary
    DiffLine.Type.CONTEXT -> MaterialTheme.colorScheme.onSurface
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(4.dp))
      .background(bgColor)
      .padding(horizontal = 6.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (line.type != DiffLine.Type.HEADER) {
      Text(
        text = line.oldLineNo?.toString() ?: " ",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        modifier = Modifier.width(26.dp)
      )
      Text(
        text = line.newLineNo?.toString() ?: " ",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        modifier = Modifier.width(26.dp)
      )
    }

    Text(
      text = line.text,
      fontFamily = FontFamily.Monospace,
      fontSize = 11.sp,
      color = textColor,
      lineHeight = 16.sp,
      modifier = Modifier.weight(1f)
    )
  }
}
