package dev.anvil.ade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

/** Tab identifiers for the build output panel. */
enum class BuildTab(val label: String) {
  BUILD("Build"),
  LOGCAT("Logcat"),
  TERMINAL("Terminal"),
  PROBLEMS("Problems"),
  DEBUG("Debug")
}

@Composable
fun BuildOutputPanel(
  buildLogs: String,
  buildErrors: List<String>,
  terminalLogs: String,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  var activeTab by remember { mutableStateOf(BuildTab.BUILD) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(AcsBuildBg)
  ) {
    // Tab bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .background(AcsSurface1)
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      BuildTab.entries.forEach { tab ->
        val isActive = activeTab == tab
        Text(
          text = tab.label,
          color = if (isActive) AcsBuildTabActive else AcsBuildTabInactive,
          fontSize = 12.sp,
          fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
          modifier = Modifier
            .clickable { activeTab = tab }
            .padding(horizontal = 12.dp, vertical = 10.dp)
        )
      }
      Spacer(Modifier.weight(1f))
      IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
        Icon(
          Icons.Default.Close,
          contentDescription = "Close panel",
          tint = AcsOnSurfaceDim,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    // Content area
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
        .background(AcsBuildBg)
        .padding(12.dp)
    ) {
      val scrollState = rememberScrollState()
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
      ) {
        when (activeTab) {
          BuildTab.BUILD -> {
            if (buildLogs.isBlank()) {
              Text("No build output yet.", color = AcsOnSurfaceDim, fontSize = 13.sp)
            } else {
              Text(
                text = buildLogs,
                color = AcsBuildText,
                fontSize = 12.sp,
                fontFamily = JetBrainsMono,
                lineHeight = 18.sp
              )
            }
          }
          BuildTab.LOGCAT -> {
            Text(
              "— waiting for device —",
              color = AcsOnSurfaceDim,
              fontSize = 13.sp,
              fontFamily = JetBrainsMono
            )
          }
          BuildTab.TERMINAL -> {
            if (terminalLogs.isBlank()) {
              Text("No terminal output yet.", color = AcsOnSurfaceDim, fontSize = 13.sp)
            } else {
              Text(
                text = terminalLogs,
                color = ForgeNeonGreen,
                fontSize = 12.sp,
                fontFamily = JetBrainsMono,
                lineHeight = 18.sp
              )
            }
          }
          BuildTab.PROBLEMS -> {
            if (buildErrors.isEmpty()) {
              Text("No problems detected.", color = AcsGreen, fontSize = 13.sp)
            } else {
              buildErrors.forEach { err ->
                Row(Modifier.padding(vertical = 2.dp)) {
                  Text("✕ ", color = AcsRed, fontSize = 12.sp, fontFamily = JetBrainsMono)
                  Text(err, color = AcsRed, fontSize = 12.sp, fontFamily = JetBrainsMono)
                }
              }
            }
          }
          BuildTab.DEBUG -> {
            Text("Debug session not active.", color = AcsOnSurfaceDim, fontSize = 13.sp)
          }
        }
      }
    }
  }
}