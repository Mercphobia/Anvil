package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
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
import dev.anvil.ade.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SdkInstallationScreen(
    onBack: () -> Unit,
    onDone: (
        sdkVersion: String,
        jdkVersion: String,
        ndkVersion: String,
        installGit: Boolean,
        installSsh: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var autoInstall by remember { mutableStateOf(true) }
    var sdkVersion by remember { mutableStateOf("35") }
    var jdkVersion by remember { mutableStateOf("21") }
    var ndkVersion by remember { mutableStateOf("27.0.12077973") }
    var installGit by remember { mutableStateOf(true) }
    var installSsh by remember { mutableStateOf(false) }

    var sdkExpanded by remember { mutableStateOf(false) }
    var jdkExpanded by remember { mutableStateOf(false) }
    var ndkExpanded by remember { mutableStateOf(false) }

    val sdkOptions = listOf("34", "35")
    val jdkOptions = listOf("17", "21", "23")
    val ndkOptions = listOf("26.3.11579264", "27.0.12077973", "28.0.12433566")

    val scrollState = rememberScrollState()

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
                "SDK Installation",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AcsOnSurface,
                fontFamily = InterDisplay
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            // Auto install toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AcsSurface2)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Auto Install",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AcsOnSurface
                    )
                    Text(
                        "Automatically download and configure SDKs",
                        fontSize = 12.sp,
                        color = AcsOnSurfaceDim
                    )
                }
                Switch(
                    checked = autoInstall,
                    onCheckedChange = { autoInstall = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = AcsGold)
                )
            }

            if (autoInstall) {
                Spacer(Modifier.height(20.dp))

                // SDK Version
                Text(
                    "SDK Version",
                    fontSize = 12.sp,
                    color = AcsOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Box {
                    OutlinedTextField(
                        value = "Android SDK $sdkVersion",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                if (sdkExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AcsOnSurfaceVariant
                            )
                        },
                        colors = sdkTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    DropdownMenu(
                        expanded = sdkExpanded,
                        onDismissRequest = { sdkExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        sdkOptions.forEach { sdk ->
                            DropdownMenuItem(
                                text = { Text("Android SDK $sdk", color = AcsOnSurface) },
                                onClick = {
                                    sdkVersion = sdk
                                    sdkExpanded = false
                                }
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { sdkExpanded = true }
                            )
                    )
                }

                Spacer(Modifier.height(16.dp))

                // JDK Version
                Text(
                    "JDK Version",
                    fontSize = 12.sp,
                    color = AcsOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Box {
                    OutlinedTextField(
                        value = "JDK $jdkVersion",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                if (jdkExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AcsOnSurfaceVariant
                            )
                        },
                        colors = sdkTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    DropdownMenu(
                        expanded = jdkExpanded,
                        onDismissRequest = { jdkExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        jdkOptions.forEach { jdk ->
                            DropdownMenuItem(
                                text = { Text("JDK $jdk", color = AcsOnSurface) },
                                onClick = {
                                    jdkVersion = jdk
                                    jdkExpanded = false
                                }
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { jdkExpanded = true }
                            )
                    )
                }

                Spacer(Modifier.height(16.dp))

                // NDK Version
                Text(
                    "NDK Version",
                    fontSize = 12.sp,
                    color = AcsOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Box {
                    OutlinedTextField(
                        value = ndkVersion,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                if (ndkExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AcsOnSurfaceVariant
                            )
                        },
                        colors = sdkTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    DropdownMenu(
                        expanded = ndkExpanded,
                        onDismissRequest = { ndkExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        ndkOptions.forEach { ndk ->
                            DropdownMenuItem(
                                text = { Text(ndk, color = AcsOnSurface) },
                                onClick = {
                                    ndkVersion = ndk
                                    ndkExpanded = false
                                }
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { ndkExpanded = true }
                            )
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Install Git toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AcsSurface2)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Install Git",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcsOnSurface
                        )
                        Text(
                            "Version control system",
                            fontSize = 12.sp,
                            color = AcsOnSurfaceDim
                        )
                    }
                    Switch(
                        checked = installGit,
                        onCheckedChange = { installGit = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = AcsGold)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Install OpenSSH toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AcsSurface2)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Install OpenSSH",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcsOnSurface
                        )
                        Text(
                            "SSH server and client tools",
                            fontSize = 12.sp,
                            color = AcsOnSurfaceDim
                        )
                    }
                    Switch(
                        checked = installSsh,
                        onCheckedChange = { installSsh = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = AcsGold)
                    )
                }
            }
        }

        // Bottom button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AcsSurface1)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    onDone(sdkVersion, jdkVersion, ndkVersion, installGit, installSsh)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AcsGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Done",
                    color = AcsBg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun sdkTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AcsGold,
    unfocusedBorderColor = AcsOutline,
    focusedTextColor = AcsOnSurface,
    unfocusedTextColor = AcsOnSurface,
    cursorColor = AcsGold,
    focusedContainerColor = AcsSurface2,
    unfocusedContainerColor = AcsSurface2
)