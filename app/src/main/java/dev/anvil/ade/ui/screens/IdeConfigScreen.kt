package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import dev.anvil.ade.ui.components.AcsSectionLabel
import dev.anvil.ade.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeConfigScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var ndkExpanded by remember { mutableStateOf(false) }
    var cmakeExpanded by remember { mutableStateOf(false) }
    var selectedNdk by remember { mutableStateOf("27.0.12077973") }
    var selectedCmake by remember { mutableStateOf("3.30.2") }

    val ndkOptions = listOf("26.3.11579264", "27.0.12077973", "28.0.12433566")
    val cmakeOptions = listOf("3.22.1", "3.28.4", "3.30.2")

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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AcsOnSurface)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "IDE Configuration",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AcsOnSurface,
                fontFamily = InterDisplay
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            AcsSectionLabel("NATIVE TOOLCHAINS")

            AcsCardGroup {
                // NDK Version selector
                Box {
                    AcsCardRow(
                        icon = Icons.Filled.Build,
                        title = "Android NDK",
                        subtitle = selectedNdk,
                        onClick = { ndkExpanded = true }
                    )
                    DropdownMenu(
                        expanded = ndkExpanded,
                        onDismissRequest = { ndkExpanded = false }
                    ) {
                        ndkOptions.forEach { ndk ->
                            DropdownMenuItem(
                                text = { Text(ndk, color = AcsOnSurface) },
                                onClick = {
                                    selectedNdk = ndk
                                    ndkExpanded = false
                                }
                            )
                        }
                    }
                }

                // NDK download button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { /* trigger NDK download */ },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AcsGold),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AcsGold)
                        )
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Download NDK", fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // CMake Version selector
                Box {
                    AcsCardRow(
                        icon = Icons.Filled.Terminal,
                        title = "CMake",
                        subtitle = selectedCmake,
                        onClick = { cmakeExpanded = true }
                    )
                    DropdownMenu(
                        expanded = cmakeExpanded,
                        onDismissRequest = { cmakeExpanded = false }
                    ) {
                        cmakeOptions.forEach { cmake ->
                            DropdownMenuItem(
                                text = { Text(cmake, color = AcsOnSurface) },
                                onClick = {
                                    selectedCmake = cmake
                                    cmakeExpanded = false
                                }
                            )
                        }
                    }
                }

                // CMake download button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { /* trigger CMake download */ },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AcsGold),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AcsGold)
                        )
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Download CMake", fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            AcsSectionLabel("BUILD SYSTEM")

            AcsCardGroup {
                AcsCardRow(
                    icon = Icons.Filled.AccountTree,
                    title = "Gradle JDK",
                    subtitle = "JDK 21 (embedded)"
                )
                AcsCardRow(
                    icon = Icons.Filled.Memory,
                    title = "Daemon Memory",
                    subtitle = "2048 MB"
                )
                AcsCardRow(
                    icon = Icons.Filled.Speed,
                    title = "Parallel Builds",
                    subtitle = "Enabled"
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                "Changes apply to the next Gradle sync.",
                fontSize = 11.sp,
                color = AcsOnSurfaceDim,
                fontFamily = JetBrainsMono,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}