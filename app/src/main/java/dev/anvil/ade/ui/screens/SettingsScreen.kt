package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.components.AcsCardGroup
import dev.anvil.ade.ui.components.AcsCardRow
import dev.anvil.ade.ui.components.AcsCardSwitchRow
import dev.anvil.ade.ui.components.AcsSectionLabel
import dev.anvil.ade.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var darkTheme by remember { mutableStateOf(true) }
    var autonomyLevel by remember { mutableStateOf(1) } // 0=safe, 1=moderate, 2=full
    var mcpServer by remember { mutableStateOf(false) }
    var hooksEnabled by remember { mutableStateOf(true) }

    val autonomyLabels = listOf("Safe", "Moderate", "Full")
    val autonomyDescriptions = listOf(
        "Agent asks before every action",
        "Agent auto-executes safe actions only",
        "Agent has full autonomous access"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AcsBg)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AcsSurface1)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = AcsOnSurface)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AcsOnSurface,
                fontFamily = InterDisplay
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            AcsSectionLabel("APPEARANCE")

            AcsCardGroup {
                AcsCardSwitchRow(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    subtitle = if (darkTheme) "Dark mode enabled" else "Light mode enabled",
                    checked = darkTheme,
                    onCheckedChange = { darkTheme = it }
                )
            }

            Spacer(Modifier.height(8.dp))

            AcsSectionLabel("AI AGENT")

            AcsCardGroup {
                // Autonomy level selector
                AcsCardRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "Autonomy Level",
                    subtitle = autonomyLabels[autonomyLevel] + " — " + autonomyDescriptions[autonomyLevel],
                    modifier = Modifier
                )
                // Autonomy chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    autonomyLabels.forEachIndexed { index, label ->
                        val isSelected = autonomyLevel == index
                        val bgColor = if (isSelected) AcsGold.copy(alpha = 0.15f) else AcsSurface3
                        val textColor = if (isSelected) AcsGold else AcsOnSurfaceVariant

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable { autonomyLevel = index }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = textColor
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))

            AcsSectionLabel("SERVICES")

            AcsCardGroup {
                AcsCardSwitchRow(
                    icon = Icons.Default.Hub,
                    title = "MCP Server",
                    subtitle = "Model Context Protocol integration",
                    checked = mcpServer,
                    onCheckedChange = { mcpServer = it }
                )
                AcsCardSwitchRow(
                    icon = Icons.Default.Link,
                    title = "Hooks",
                    subtitle = "Pre/post action automation hooks",
                    checked = hooksEnabled,
                    onCheckedChange = { hooksEnabled = it }
                )
            }

            Spacer(Modifier.height(8.dp))

            AcsSectionLabel("ABOUT")

            AcsCardGroup {
                AcsCardRow(
                    icon = Icons.Default.Info,
                    title = "Anvil Studio",
                    subtitle = "Version 1.0.0-dev • Built for Android"
                )
            }
        }
    }
}