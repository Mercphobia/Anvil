package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.components.AcsCardGroup
import dev.anvil.ade.ui.components.AcsCardRow
import dev.anvil.ade.ui.components.AcsSectionLabel
import dev.anvil.ade.ui.theme.*

@Composable
fun PreferencesScreen(
    onBack: () -> Unit,
    onOpenGeneral: () -> Unit = {},
    onOpenEditor: () -> Unit = {},
    onOpenAiAgent: () -> Unit = {},
    onOpenBuildRun: () -> Unit = {},
    onOpenTermux: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    onOpenDevOptions: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                Icon(Icons.Filled.ArrowBack, "Back", tint = AcsOnSurface)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "IDE Preferences",
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
            AcsSectionLabel("CONFIGURE")

            AcsCardGroup {
                AcsCardRow(
                    icon = Icons.Filled.Tune,
                    title = "General",
                    subtitle = "Appearance, language, startup",
                    onClick = onOpenGeneral
                )
                AcsCardRow(
                    icon = Icons.Filled.Code,
                    title = "Editor",
                    subtitle = "Font, line numbers, word wrap, indentation",
                    onClick = onOpenEditor
                )
                AcsCardRow(
                    icon = Icons.Filled.AutoAwesome,
                    title = "AI Agent",
                    subtitle = "Model provider, autonomy, permissions",
                    onClick = onOpenAiAgent
                )
                AcsCardRow(
                    icon = Icons.Filled.PlayArrow,
                    title = "Build & Run",
                    subtitle = "Gradle JDK, daemon memory, compiler flags",
                    onClick = onOpenBuildRun
                )
                AcsCardRow(
                    icon = Icons.Filled.Terminal,
                    title = "Termux",
                    subtitle = "Shell integration, environment variables",
                    onClick = onOpenTermux
                )
            }

            Spacer(Modifier.height(8.dp))

            AcsSectionLabel("SYSTEM")

            AcsCardGroup {
                AcsCardRow(
                    icon = Icons.Filled.Security,
                    title = "Privacy",
                    subtitle = "Telemetry, crash reports, data sharing",
                    onClick = onOpenPrivacy
                )
            }

            Spacer(Modifier.height(8.dp))

            AcsSectionLabel("MORE")

            AcsCardGroup {
                AcsCardRow(
                    icon = Icons.Filled.BugReport,
                    title = "Developer Options",
                    subtitle = "Debug overlay, layout bounds, GPU profiling",
                    onClick = onOpenDevOptions
                )
                AcsCardRow(
                    icon = Icons.Filled.Info,
                    title = "About",
                    subtitle = "Version, licenses, open-source credits",
                    onClick = onOpenAbout
                )
            }
        }
    }
}