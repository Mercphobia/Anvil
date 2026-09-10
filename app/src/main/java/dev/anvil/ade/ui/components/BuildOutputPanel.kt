package dev.anvil.ade.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

data class BuildOutputTab(val id: String, val label: String)
data class BuildLogEntry(val line: String, val isError: Boolean = false, val isWarning: Boolean = false)

@Composable
fun BuildOutputPanel(
  logs: List<BuildLogEntry>,
  isBuilding: Boolean,
  onRetry: () -> Unit,
  onClear: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(0) }
  var expanded by remember { mutableStateOf(false) }

  val tabs = listOf(
    BuildOutputTab("build", "Build output"),
    BuildOutputTab("applogs", "App Logs"),
    BuildOutputTab("terminal", "Terminal"),
    BuildOutputTab("ide", "IDE Logs"),
    BuildOutputTab("diag", "Diagnostics")
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
      .background(AcsBuildBg)
      .animateContentSize()
  ) {
    // Drag handle
    Box(
      modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .width(36.dp).height(4.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
      )
    }

    // Tab row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .clickable { expanded = !expanded }
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      tabs.forEachIndexed { idx, tab ->
        val isSelected = selectedTab == idx
        Text(
          tab.label,
          fontSize = 11.sp,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
          color = if (isSelected) AcsBuildTabActive else AcsBuildTabInactive,
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) AcsGold.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { selectedTab = idx }
            .padding(horizontal = 10.dp, vertical = 5.dp)
        )
      }
    }

    // Log content
    if (expanded) {
      HorizontalDivider(color = AcsOutlineVariant.copy(alpha = 0.3f))
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 80.dp, max = 300.dp)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 14.dp, vertical = 8.dp)
      ) {
        if (isBuilding) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = AcsGold)
            Spacer(Modifier.width(8.dp))
            Text("Starting build...", fontSize = 12.sp, color = AcsBuildText)
          }
        }

        if (logs.isEmpty() && !isBuilding) {
          Text("No build output yet.", fontSize = 12.sp, color = AcsBuildText, modifier = Modifier.padding(top = 8.dp))
        }

        logs.forEachIndexed { idx, entry ->
          Text(
            text = "${idx + 1}  ${entry.line}",
            fontSize = 11.sp,
            fontFamily = JetBrainsMono,
            color = when {
              entry.isError -> AcsRed
              entry.isWarning -> AcsGold
              else -> AcsBuildText
            },
            lineHeight = 16.sp,
            modifier = Modifier.padding(vertical = 1.dp)
          )
        }
      }

      // Action row
      if (logs.isNotEmpty() || isBuilding) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          TextButton(onClick = onRetry, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
            Icon(Icons.Filled.Refresh, null, tint = AcsTeal, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Retry", fontSize = 11.sp, color = AcsTeal)
          }
          TextButton(onClick = onClear, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
            Icon(Icons.Filled.Delete, null, tint = AcsOnSurfaceDim, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Clear", fontSize = 11.sp, color = AcsOnSurfaceDim)
          }
          Spacer(Modifier.weight(1f))
          TextButton(onClick = { expanded = false }) {
            Text("Collapse", fontSize = 11.sp, color = AcsOnSurfaceDim)
          }
        }
      }
    }
  }
}