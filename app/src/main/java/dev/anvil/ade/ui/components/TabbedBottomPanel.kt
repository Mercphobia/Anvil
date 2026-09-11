package dev.anvil.ade.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.compiler.BuildPipelineManager
import dev.anvil.ade.ui.theme.GeistMono
import dev.anvil.ade.ui.theme.LocalThemeTokens

/**
 * Tabbed bottom panel — Build | Logs | Terminal | Diagnostics.
 * Blueprint 3.1: swipe-up panel with ruler-tick handle, amber underline
 * active tab, blueprintCorners. Replaces the old "Build merged into Terminal"
 * approach with separate tabs in one collapsible panel.
 */
@Composable
fun TabbedBottomPanel(
    buildLogs: String,
    buildErrors: List<BuildPipelineManager.BuildError>,
    terminalLogs: String,
    isBuilding: Boolean,
    onRetryBuild: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) }
    val tabLabels = listOf("BUILD", "LOGS", "TERMINAL", "DIAGNOSTICS")
    val tokens = LocalThemeTokens.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column {
            // Ruler-tick drag handle
            RulerTickDragHandle(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 6.dp)
            )

            // Tab bar — horizontal scroll, amber underline 2dp on active
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                tabLabels.forEachIndexed { index, label ->
                    val isActive = activeTab == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontFamily = GeistMono,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .clickable { activeTab = index }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                        // Amber underline 2dp for active tab (Blueprint spec)
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(2.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Tab content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    when (activeTab) {
                        0 -> { // BUILD
                            if (isBuilding) {
                                Text("Building...", color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp, fontFamily = GeistMono)
                                Spacer(Modifier.height(8.dp))
                            }
                            if (buildLogs.isBlank()) {
                                Text("No build output yet. Swipe up after running a build.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            } else {
                                Text(text = buildLogs, color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp, fontFamily = GeistMono, lineHeight = 18.sp)
                            }
                        }
                        1 -> { // LOGS
                            Text("— app logs (logcat) —", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp, fontFamily = GeistMono)
                            Spacer(Modifier.height(8.dp))
                            Text("Connect a device or emulator to see runtime logs.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        2 -> { // TERMINAL
                            if (terminalLogs.isBlank()) {
                                Text("No terminal output yet.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp)
                            } else {
                                Text(text = terminalLogs, color = tokens.ok,
                                    fontSize = 12.sp, fontFamily = GeistMono, lineHeight = 18.sp)
                            }
                        }
                        3 -> { // DIAGNOSTICS
                            if (buildErrors.isEmpty()) {
                                Text("No issues detected.", color = tokens.ok,
                                    fontSize = 13.sp, fontFamily = GeistMono)
                            } else {
                                Text("${buildErrors.size} build error(s):",
                                    color = tokens.err, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                buildErrors.forEach { err ->
                                    Row(Modifier.padding(vertical = 2.dp)) {
                                        Text("\u2715 ", color = tokens.err, fontSize = 12.sp, fontFamily = GeistMono)
                                        Text("${err.file}:${err.line}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp, fontFamily = GeistMono)
                                    }
                                    Text(err.message, color = tokens.err,
                                        fontSize = 11.sp, fontFamily = GeistMono,
                                        modifier = Modifier.padding(start = 18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}