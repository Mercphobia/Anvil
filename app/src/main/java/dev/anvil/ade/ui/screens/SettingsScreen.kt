package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*
import dev.anvil.ade.ui.components.AcsCardGroup
import dev.anvil.ade.ui.components.AcsCardRow
import dev.anvil.ade.ui.components.AcsCardSwitchRow
import dev.anvil.ade.ui.components.AcsSectionLabel

/**
 * Settings toggles for ecosystem features:
 *   - Theme engine (dark/light)
 *   - Autonomy level (safe / moderate / full)
 *   - MCP server on/off
 *   - Hook engine on/off
 *
 * Integrates into the Preferences flow as a standalone screen reachable
 * from PreferencesScreen or directly from the bottom nav.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    themeDark: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    autonomyLevel: String,
    onAutonomyChange: (String) -> Unit,
    mcpEnabled: Boolean,
    onMcpToggle: (Boolean) -> Unit,
    hooksEnabled: Boolean,
    onHooksToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "Settings",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Spacer(Modifier.height(4.dp))

        // ── Appearance ───────────────────────────────────────────────
        AcsSectionLabel("Appearance", modifier = Modifier.padding(horizontal = 16.dp))
        AcsCardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
            AcsCardSwitchRow(
                icon = Icons.Filled.DarkMode,
                title = "Dark theme",
                subtitle = if (themeDark) "Using dark color scheme" else "Using light color scheme",
                checked = themeDark,
                onCheckedChange = onThemeToggle,
                accent = AcsTeal
            )
        }

        // ── Agent Behavior ───────────────────────────────────────────
        AcsSectionLabel("Agent Behavior", modifier = Modifier.padding(horizontal = 16.dp))
        AcsCardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {

            // Autonomy level — cycle through safe/moderate/full
            val autonomyLabel = when (autonomyLevel) {
                "full" -> "Full"
                "moderate" -> "Moderate"
                else -> "Safe"
            }
            val autonomySubtitle = when (autonomyLevel) {
                "full" -> "Agent executes write/commit/terminal freely"
                "moderate" -> "Agent asks before risky actions"
                else -> "Agent requires approval for all file/terminal changes"
            }
            val autonomyIcon = when (autonomyLevel) {
                "full" -> Icons.Filled.Bolt
                "moderate" -> Icons.Filled.Shield
                else -> Icons.Filled.Lock
            }

            AcsCardRow(
                icon = autonomyIcon,
                title = "Autonomy level",
                subtitle = autonomySubtitle,
                accent = when (autonomyLevel) {
                    "full" -> AcsRed
                    "moderate" -> AcsGold
                    else -> AcsGreen
                },
                onClick = {
                    val next = when (autonomyLevel) {
                        "safe" -> "moderate"
                        "moderate" -> "full"
                        "full" -> "safe"
                        else -> "safe"
                    }
                    onAutonomyChange(next)
                },
                trailing = {
                    Text(
                        text = autonomyLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (autonomyLevel) {
                            "full" -> AcsRed
                            "moderate" -> AcsGold
                            else -> AcsGreen
                        }
                    )
                }
            )
        }

        // ── Ecosystem ────────────────────────────────────────────────
        AcsSectionLabel("Ecosystem", modifier = Modifier.padding(horizontal = 16.dp))

        AcsCardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
            AcsCardSwitchRow(
                icon = Icons.Filled.Hub,
                title = "MCP server",
                subtitle = if (mcpEnabled) "MCP tools exposed on localhost:9876" else "External MCP clients cannot connect",
                checked = mcpEnabled,
                onCheckedChange = onMcpToggle,
                accent = AcsTeal
            )

            AcsCardSwitchRow(
                icon = Icons.Filled.ElectricBolt,
                title = "Hook engine",
                subtitle = if (hooksEnabled) ".anvil/hooks/*.json watchers active" else "File-system hooks are disabled",
                checked = hooksEnabled,
                onCheckedChange = onHooksToggle,
                accent = AcsGold
            )
        }

        // ── Info footer ──────────────────────────────────────────────
        Spacer(Modifier.height(24.dp))
        Text(
            text = "MCP server and Hook engine are opt-in features. They remain off until you toggle them here. Risky actions always require your approval unless autonomy is set to Full.",
            fontSize = 11.sp,
            color = AcsOnSurfaceDim,
            modifier = Modifier.padding(horizontal = 20.dp),
            lineHeight = 16.sp
        )
        Spacer(Modifier.height(16.dp))
    }
}