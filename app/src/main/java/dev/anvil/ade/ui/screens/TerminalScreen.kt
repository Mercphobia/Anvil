package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.viewmodel.AnvilViewModel

@Composable
fun TerminalScreen(
  viewModel: AnvilViewModel,
  modifier: Modifier = Modifier
) {
  val terminalLogs by viewModel.terminalLogs.collectAsState()
  val isRunning by viewModel.isTerminalRunning.collectAsState()
  val branch by viewModel.gitBranch.collectAsState()

  var inputCmd by remember { mutableStateOf("") }
  val scrollState = rememberScrollState()

  // Auto scroll to bottom when log changes
  LaunchedEffect(terminalLogs) {
    scrollState.animateScrollTo(scrollState.maxValue)
  }

  val commandTemplates = remember {
    listOf(
      "git status" to "Git Status",
      "git diff" to "Git Diff",
      "git log -n 3" to "Git Log",
      "aapt2 compile" to "aapt2 Compile",
      "ecj -cp android.jar" to "ECJ Java",
      "d8 --release" to "D8 Dex",
      "apksigner verify" to "Verify APK",
      "pm install -r app.apk" to "PM Install",
      "tree" to "Working Tree",
      "help" to "Help"
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Top Bar Info: Monospace prompt info + Branch + Clear action
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.Terminal,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = branch,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(
        onClick = { viewModel.clearTerminal() },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Filled.Clear,
          contentDescription = "Clear Terminal",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    // Quick Command Template Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      commandTemplates.forEach { (cmd, label) ->
        SuggestionChip(
          onClick = {
            viewModel.runTerminalCommand(cmd)
          },
          label = {
            Text(
              text = label,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Medium
            )
          },
          shape = RoundedCornerShape(8.dp),
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
          ),
          enabled = !isRunning
        )
      }
    }

    // Terminal Screen Window (Sleek Dark Canvas with Monospace text)
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .clip(RoundedCornerShape(10.dp))
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
      color = Color(0xFF0A0C10)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(scrollState)
        ) {
          // Terminal Output Text (selectable for copy)
          androidx.compose.foundation.text.selection.SelectionContainer {
            Text(
              text = terminalLogs,
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              lineHeight = 18.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        if (isRunning) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(12.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Monospace Command Input Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = inputCmd,
        onValueChange = { inputCmd = it },
        placeholder = {
          Text(
            text = "sh — ketik perintah (git status, npm run, …)",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
          )
        },
        leadingIcon = {
          Text(
            text = "\u276F",
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
          )
        },
        modifier = Modifier
          .weight(1f)
          .testTag("terminal_input_field"),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
          onSend = {
            if (inputCmd.isNotBlank() && !isRunning) {
              viewModel.runTerminalCommand(inputCmd)
              inputCmd = ""
            }
          }
        ),
        enabled = !isRunning
      )

      Spacer(modifier = Modifier.width(8.dp))

      Button(
        onClick = {
          if (inputCmd.isNotBlank() && !isRunning) {
            viewModel.runTerminalCommand(inputCmd)
            inputCmd = ""
          }
        },
        modifier = Modifier
          .size(44.dp)
          .testTag("terminal_send_button"),
        shape = RoundedCornerShape(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        enabled = !isRunning && inputCmd.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Icon(
          imageVector = Icons.Filled.PlayArrow,
          contentDescription = "Run Command",
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
