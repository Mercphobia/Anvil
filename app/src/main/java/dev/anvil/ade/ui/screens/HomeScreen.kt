package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.components.AcsCardGroup
import dev.anvil.ade.ui.components.AcsCardRow
import dev.anvil.ade.ui.components.AcsSectionLabel
import dev.anvil.ade.ui.theme.*

@Composable
fun HomeScreen(
    onCreateProject: () -> Unit,
    onOpenProject: () -> Unit,
    onCloneGit: () -> Unit,
    onOpenTerminal: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenIdeConfig: () -> Unit,
    onOpenDocs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AcsBg)
            .padding(top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero logo mark
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(AcsGold, AcsTeal))),
            contentAlignment = Alignment.Center
        ) {
            Text("⚒", fontSize = 32.sp)
        }

        Spacer(Modifier.height(20.dp))

        // Headline
        Text(
            "Android Code Studio",
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            color = AcsOnSurface,
            textAlign = TextAlign.Center,
            fontFamily = InterDisplay,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "Your Ideas, Anywhere.",
            fontSize = 15.sp,
            color = AcsGold,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(28.dp))

        // Action rows
        AcsCardGroup {
            AcsCardRow(
                icon = Icons.Filled.Add,
                title = "Create Project",
                subtitle = "Start fresh from a template",
                onClick = onCreateProject
            )
            AcsCardRow(
                icon = Icons.Filled.FolderOpen,
                title = "Open Project",
                subtitle = "Open an existing local project",
                onClick = onOpenProject
            )
            AcsCardRow(
                icon = Icons.Outlined.CloudDownload,
                title = "Clone Git Repo",
                subtitle = "Clone from GitHub, GitLab or any remote",
                onClick = onCloneGit
            )
            AcsCardRow(
                icon = Icons.Filled.Terminal,
                title = "Open Terminal",
                subtitle = "Direct shell access to your environment",
                onClick = onOpenTerminal
            )
            AcsCardRow(
                icon = Icons.Filled.Settings,
                title = "Preferences",
                subtitle = "Editor, keymap, and workspace settings",
                onClick = onOpenPreferences
            )
            AcsCardRow(
                icon = Icons.Filled.Build,
                title = "IDE Configuration",
                subtitle = "SDK, NDK, CMake and toolchains",
                onClick = onOpenIdeConfig
            )
            AcsCardRow(
                icon = Icons.Filled.MenuBook,
                title = "Documentation",
                subtitle = "Guides, API reference, and tutorials",
                onClick = onOpenDocs
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            "Anvil Studio • v1.0.0-dev",
            fontSize = 11.sp,
            color = AcsOnSurfaceDim,
            fontFamily = JetBrainsMono
        )
    }
}