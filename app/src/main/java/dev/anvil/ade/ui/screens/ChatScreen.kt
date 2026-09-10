package dev.anvil.ade.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.model.ProjectType
import dev.anvil.ade.model.AgentStep
import dev.anvil.ade.model.StepKind
import dev.anvil.ade.viewmodel.AnvilViewModel

@Composable
fun ChatScreen(
  viewModel: AnvilViewModel,
  modifier: Modifier = Modifier
) {
  val steps by viewModel.steps.collectAsState()
  val isBusy by viewModel.isBusy.collectAsState()
  val activeType by viewModel.activeType.collectAsState()
  val providerConfig by viewModel.providerConfig.collectAsState()
  val pendingMemory by viewModel.pendingMemory.collectAsState()
  val pendingSkillProposal by viewModel.pendingSkillProposal.collectAsState()
  val pendingTerminalCommand by viewModel.pendingTerminalCommand.collectAsState()
  val pendingSoulProposal by viewModel.pendingSoulProposal.collectAsState()

  var inputText by remember { mutableStateOf("") }
  var showAttachMenu by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()
  val clipboardManager = LocalClipboardManager.current

  LaunchedEffect(steps.size) {
    if (steps.isNotEmpty()) {
      listState.animateScrollToItem(steps.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.surface)
  ) {
    // Gemini Top Header: Model Pill & Mode Indicator
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Gemini Model Dropdown Pill
      Surface(
        onClick = { viewModel.toggleSettingsDialog(true) },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.testTag("gemini_model_selector")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Gemini 2.5 Flash",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Pilih Model",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      // Mode Indicator (read-only - mode auto-detected from how the project
      // was opened: local = App Builder, cloned repo = SystemUI)
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (activeType == ProjectType.ANDROID)
          MaterialTheme.colorScheme.primaryContainer
        else
          MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.testTag("mode_indicator")
      ) {
        Text(
          text = if (activeType == ProjectType.ANDROID) "App Builder" else "SystemUI",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp,
          color = if (activeType == ProjectType.ANDROID)
            MaterialTheme.colorScheme.onPrimaryContainer
          else
            MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }

    // Main Chat Message Area or Gemini Zero State
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      if (steps.isEmpty()) {
        // Iconic Gemini Empty Greeting State
        GeminiEmptyState(
          onSelectPrompt = { inputText = it },
          activeType = activeType
        )
      } else {
        LazyColumn(
          state = listState,
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          items(steps, key = { it.id }) { step ->
            when (step.kind) {
              StepKind.USER -> GeminiUserMessage(step)
              StepKind.AGENT_TEXT -> GeminiAgentMessage(
                step = step,
                onCopy = { clipboardManager.setText(AnnotatedString(step.text)) },
                onOpenEditor = { viewModel.setRoute("editor") }
              )
              StepKind.TOOL_CALL -> GeminiToolCallStep(step)
              StepKind.TOOL_RESULT -> GeminiToolResultStep(step)
              StepKind.INFO -> GeminiInfoCard(step)
              StepKind.ERROR -> GeminiErrorCard(step)
            }
          }

          if (isBusy) {
            item {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                CircularProgressIndicator(
                  modifier = Modifier.size(14.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Gemini sedang memproses & menyusun kode...",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // Skill Update Approval Pill if pending
    AnimatedVisibility(
      visible = pendingSkillProposal != null,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      pendingSkillProposal?.let { proposal ->
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Update Skill: " + proposal.slug,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = proposal.reason,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              TextButton(onClick = { viewModel.dismissSkillProposal() }) {
                Text("Abaikan", fontSize = 12.sp)
              }
              Spacer(modifier = Modifier.width(4.dp))
              Button(
                onClick = { viewModel.confirmSkillProposal() },
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Setujui", fontSize = 12.sp)
              }
            }
          }
        }
      }
    }

    // Terminal Command Confirmation Pill if pending
    AnimatedVisibility(
      visible = pendingTerminalCommand != null,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      pendingTerminalCommand?.let { cmd ->
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Jalankan perintah shell?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = cmd,
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              TextButton(onClick = { viewModel.dismissTerminalCommand() }) {
                Text("Batal", fontSize = 12.sp)
              }
              Spacer(modifier = Modifier.width(4.dp))
              Button(
                onClick = { viewModel.confirmTerminalCommand() },
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Jalankan", fontSize = 12.sp)
              }
            }
          }
        }
      }
    }

    // Soul Update Confirmation Pill if pending
    AnimatedVisibility(
      visible = pendingSoulProposal != null,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      pendingSoulProposal?.let { proposal ->
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Update SOUL.md agent?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = proposal.reason,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              TextButton(onClick = { viewModel.dismissSoulProposal() }) {
                Text("Abaikan", fontSize = 12.sp)
              }
              Spacer(modifier = Modifier.width(4.dp))
              Button(
                onClick = { viewModel.confirmSoulProposal() },
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Terapkan", fontSize = 12.sp)
              }
            }
          }
        }
      }
    }

    // Memory Confirmation Pill if pending
    AnimatedVisibility(
      visible = pendingMemory != null,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      pendingMemory?.let { memoryText ->
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Memory,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Simpan ke Memory?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = memoryText,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              TextButton(onClick = { viewModel.dismissMemory() }) {
                Text("Abaikan", fontSize = 12.sp)
              }
              Spacer(modifier = Modifier.width(4.dp))
              Button(
                onClick = { viewModel.confirmMemory() },
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Simpan", fontSize = 12.sp)
              }
            }
          }
        }
      }
    }

    // Horizontal Suggestion Chips above the Prompt Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val suggestions = if (activeType == ProjectType.ANDROID) {
        listOf(
          "Buat kalkulator faktorial",
          "Tambah Empty Activity baru",
          "Kompilasi APK via D8 & AAPT2",
          "Periksa error log terminal"
        )
      } else {
        listOf(
          "Harmonisasi QS Tile Monet",
          "Tambah tile Internet & Hotspot",
          "Periksa syntax layout XML",
          "Commit perubahan ke Git"
        )
      }

      suggestions.forEach { prompt ->
        SuggestionChip(
          onClick = { inputText = prompt },
          label = { Text(prompt, fontSize = 11.sp) },
          shape = RoundedCornerShape(16.dp),
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
          )
        )
      }
    }

    // Gemini Floating Bottom Input Bar (Capsule Pill Shape)
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 2.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Attachment / Action Plus Button
        Box {
          IconButton(
            onClick = { showAttachMenu = true },
            modifier = Modifier.size(38.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = "Opsi Tambahan",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }

          DropdownMenu(
            expanded = showAttachMenu,
            onDismissRequest = { showAttachMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Pilih Template Proyek") },
              leadingIcon = { Icon(Icons.Filled.Code, contentDescription = null) },
              onClick = {
                showAttachMenu = false
                viewModel.toggleTemplateDialog(true)
              }
            )
            DropdownMenuItem(
              text = { Text("Buka Proyek (Lokal / GitHub)") },
              leadingIcon = { Icon(Icons.Filled.FolderOpen, contentDescription = null) },
              onClick = {
                showAttachMenu = false
                viewModel.toggleOpenProjectDialog(true)
              }
            )
            DropdownMenuItem(
              text = { Text("Jalankan Terminal On-Device") },
              leadingIcon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
              onClick = {
                showAttachMenu = false
                viewModel.setRoute("terminal")
              }
            )
          }
        }

        // Gemini Prompt Input Field
        BasicTextField(
          value = inputText,
          onValueChange = { inputText = it },
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .testTag("chat_input_field"),
          textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
          ),
          cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
          decorationBox = { innerTextField ->
            if (inputText.isEmpty()) {
              Text(
                text = "Tanya Gemini...",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 14.sp
              )
            }
            innerTextField()
          }
        )

        // Mic or Send Button
        if (inputText.isNotBlank()) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary)
              .clickable {
                val prompt = inputText.trim()
                if (prompt.isNotEmpty()) {
                  viewModel.sendMessage(prompt)
                  inputText = ""
                }
              }
              .testTag("chat_send_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Kirim",
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(18.dp)
            )
          }
        } else {
          IconButton(
            onClick = {
              inputText = "Analisis status proyek dan rekomendasikan langkah berikutnya"
            },
            modifier = Modifier.size(38.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Mic,
              contentDescription = "Voice Prompt",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun GeminiEmptyState(
  onSelectPrompt: (String) -> Unit,
  activeType: ProjectType
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.Center
  ) {
    // Gemini Sparkle & Greeting
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              listOf(Color(0xFF4285F4), Color(0xFF9B51E0), Color(0xFFEA4335))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.AutoAwesome,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "Halo, developer",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = if (activeType == ProjectType.ANDROID) {
        "Apa yang ingin Anda bangun hari ini?"
      } else {
        "Desain SystemUI apa yang ingin Anda modifikasi?"
      },
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      letterSpacing = (-0.5).sp,
      lineHeight = 34.sp
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Prompt Card Suggestions
    val promptCards = if (activeType == ProjectType.ANDROID) {
      listOf(
        "Buat Empty Activity dengan Jetpack Compose & Material 3",
        "Bangun aplikasi kalkulator ilmiah sederhana dengan Java",
        "Kompilasi source code menjadi APK mandiri di ponsel",
        "Periksa deklarasi permission di AndroidManifest.xml"
      )
    } else {
      listOf(
        "Harmonisasikan Quick Settings panel dengan warna Monet",
        "Buat overlay status bar transparan untuk AOSP",
        "Validasi XML layout agar aman dari private @*android: ID",
        "Bandingkan git diff dan buat commit baru"
      )
    }

    promptCards.chunked(2).forEach { rowPrompts ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        rowPrompts.forEach { prompt ->
          Card(
            onClick = { onSelectPrompt(prompt) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = prompt,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun GeminiUserMessage(step: AgentStep) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.End
  ) {
    Surface(
      shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      modifier = Modifier.fillMaxWidth(0.85f)
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
          text = step.text,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 20.sp
        )
      }
    }
  }
}

@Composable
private fun GeminiAgentMessage(
  step: AgentStep,
  onCopy: () -> Unit,
  onOpenEditor: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.Start
  ) {
    // Gemini Spark Icon
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .background(
          Brush.linearGradient(
            listOf(Color(0xFF4285F4), Color(0xFF9B51E0), Color(0xFFEA4335))
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.AutoAwesome,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(14.dp)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = step.text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 22.sp
      )

      // Gemini Response Quick Actions Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
          Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = "Salin tanggapan",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
        }

        IconButton(onClick = onOpenEditor, modifier = Modifier.size(28.dp)) {
          Icon(
            imageVector = Icons.Filled.Code,
            contentDescription = "Buka di Editor",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
        }

        IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
          Icon(
            imageVector = Icons.Filled.ThumbUp,
            contentDescription = "Suka",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun GeminiToolCallStep(step: AgentStep) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 36.dp, top = 2.dp, bottom = 2.dp),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Filled.Code,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = step.text,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.weight(1f)
      )
      step.executionMs?.let { ms ->
        Text(
          text = "${ms}ms",
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.outline
        )
      }
    }
  }
}

@Composable
private fun GeminiToolResultStep(step: AgentStep) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 44.dp, top = 1.dp, bottom = 2.dp),
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceContainer
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Filled.Check,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(12.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = step.text,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun GeminiInfoCard(step: AgentStep) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 36.dp, top = 2.dp),
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
  ) {
    Text(
      text = step.text,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSecondaryContainer,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}

@Composable
private fun GeminiErrorCard(step: AgentStep) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = 36.dp, top = 2.dp),
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.errorContainer
  ) {
    Text(
      text = "Error: ${step.text}",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onErrorContainer,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}
