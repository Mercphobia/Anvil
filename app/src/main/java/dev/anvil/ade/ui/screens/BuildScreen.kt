package dev.anvil.ade.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.ForgeNeonGreen
import dev.anvil.ade.ui.theme.ForgeTerminalBg
import dev.anvil.ade.viewmodel.AnvilViewModel

@Composable
fun BuildScreen(
  viewModel: AnvilViewModel,
  modifier: Modifier = Modifier
) {
  val isBuilding by viewModel.isBuilding.collectAsState()
  val stage by viewModel.buildStage.collectAsState()
  val logs by viewModel.buildLogs.collectAsState()
  val scrollState = rememberScrollState()

  LaunchedEffect(logs) {
    scrollState.animateScrollTo(scrollState.maxValue)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Pipeline Header Card
    ElevatedCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
      )
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Android,
              contentDescription = "APK Pipeline",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "On-Device Build Pipeline",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "ARM64 Toolchain (aapt2 -> ecj -> d8 -> sign)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Button(
            onClick = { viewModel.runBuildPipeline() },
            enabled = !isBuilding,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("run_build_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            )
          ) {
            if (isBuilding) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Building...")
            } else {
              Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Build APK")
            }
          }
        }

        if (isBuilding) {
          Spacer(modifier = Modifier.height(12.dp))
          LinearProgressIndicator(
            progress = { stage / 5f },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5 Stage Indicator Pills
        val stages = listOf(
          "1. AAPT2 Link",
          "2. ECJ Compile",
          "3. D8 Dex",
          "4. APK Signer",
          "5. Install"
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          stages.forEachIndexed { index, name ->
            val stepNo = index + 1
            val isDone = stage > stepNo || (!isBuilding && stage == 5)
            val isCurrent = isBuilding && stage == stepNo

            val pillBg = when {
              isDone -> MaterialTheme.colorScheme.primaryContainer
              isCurrent -> MaterialTheme.colorScheme.secondaryContainer
              else -> MaterialTheme.colorScheme.surfaceContainer
            }

            val textColor = when {
              isDone -> MaterialTheme.colorScheme.onPrimaryContainer
              isCurrent -> MaterialTheme.colorScheme.onSecondaryContainer
              else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = pillBg
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (isDone) {
                  Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                  text = name,
                  fontSize = 11.sp,
                  fontWeight = if (isCurrent || isDone) FontWeight.Bold else FontWeight.Medium,
                  color = textColor
                )
              }
            }
          }
        }
      }
    }

    // Terminal Log Console
    ElevatedCard(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .testTag("build_terminal_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
      )
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Console Title Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 14.dp, vertical = 8.dp),
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
              text = "Console Output (stdout / stderr)",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isBuilding) MaterialTheme.colorScheme.primary else ForgeNeonGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isBuilding) "BUSY" else "IDLE",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Terminal Log text
        Box(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
        ) {
          Text(
            text = logs,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}
