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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.LocalThemeTokens
import dev.anvil.ade.ui.theme.GeistMono

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
  val tokens = LocalThemeTokens.current

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceContainerLowest)
  ) {
    // Tab bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      BuildTab.entries.forEach { tab ->
        val isActive = activeTab == tab
        Text(
          text = tab.label,
          color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 12.sp,
          fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
          modifier = Modifier
            .clickable { activeTab = tab }
            .padding(horizontal = 12.dp, vertical = 10.dp)
        )
      }
      Spacer(Modifier.weight(1f))
      IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
        Icon(Icons.Filled.Close, contentDescription = "Close panel",
          tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
      }
    }

    // Content area
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        .padding(12.dp)
    ) {
      val scrollState = rememberScrollState()
      Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        when (activeTab) {
          BuildTab.BUILD -> {
            if (buildLogs.isBlank()) {
              Text("No build output yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            } else {
              Text(text = buildLogs, color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp, fontFamily = GeistMono, lineHeight = 18.sp)
            }
          }
          BuildTab.LOGCAT -> {
            Text("— waiting for device —", color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 13.sp, fontFamily = GeistMono)
          }
          BuildTab.TERMINAL -> {
            if (terminalLogs.isBlank()) {
              Text("No terminal output yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            } else {
              Text(text = terminalLogs, color = tokens.ok,
                fontSize = 12.sp, fontFamily = GeistMono, lineHeight = 18.sp)
            }
          }
          BuildTab.PROBLEMS -> {
            if (buildErrors.isEmpty()) {
              Text("No problems detected.", color = tokens.ok, fontSize = 13.sp)
            } else {
              buildErrors.forEach { err ->
                Row(Modifier.padding(vertical = 2.dp)) {
                  Text("✕ ", color = tokens.err, fontSize = 12.sp, fontFamily = GeistMono)
                  Text(err, color = tokens.err, fontSize = 12.sp, fontFamily = GeistMono)
                }
              }
            }
          }
          BuildTab.DEBUG -> {
            Text("Debug session not active.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
          }
        }
      }
    }
  }
}