package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectConfigScreen(
    onBack: () -> Unit,
    onCreateProject: (
        name: String,
        packageName: String,
        location: String,
        language: String,
        minSdk: String,
        useKts: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var projectName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("com.example.myapp") }
    var projectLocation by remember { mutableStateOf("/storage/emulated/0/AnvilProjects") }
    var language by remember { mutableStateOf("Kotlin") }
    var minSdk by remember { mutableStateOf("24") }
    var useKts by remember { mutableStateOf(true) }
    var languageExpanded by remember { mutableStateOf(false) }
    var sdkExpanded by remember { mutableStateOf(false) }

    val languageOptions = listOf("Kotlin", "Java")
    val sdkOptions = listOf("21", "24", "26", "29", "31", "34", "35")

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
                "New Project",
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
            // Project name
            ConfigField(
                label = "Project Name",
                placeholder = "MyApp",
                value = projectName,
                onValueChange = { projectName = it }
            )

            Spacer(Modifier.height(16.dp))

            // Package name
            ConfigField(
                label = "Package Name",
                placeholder = "com.example.myapp",
                value = packageName,
                onValueChange = { packageName = it }
            )

            Spacer(Modifier.height(16.dp))

            // Project location
            ConfigField(
                label = "Project Location",
                placeholder = "/storage/emulated/0/AnvilProjects",
                value = projectLocation,
                onValueChange = { projectLocation = it }
            )

            Spacer(Modifier.height(16.dp))

            // Language dropdown
            Text("Language", fontSize = 12.sp, color = AcsOnSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Box {
                OutlinedTextField(
                    value = language,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            if (languageExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = AcsOnSurfaceVariant
                        )
                    },
                    colors = acsTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                DropdownMenu(
                    expanded = languageExpanded,
                    onDismissRequest = { languageExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.88f)
                ) {
                    languageOptions.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang, color = AcsOnSurface) },
                            onClick = {
                                language = lang
                                languageExpanded = false
                            }
                        )
                    }
                }
                // Transparent overlay to capture tap
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { languageExpanded = true }
                        )
                )
            }

            Spacer(Modifier.height(16.dp))

            // Minimum SDK dropdown
            Text("Minimum SDK", fontSize = 12.sp, color = AcsOnSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Box {
                OutlinedTextField(
                    value = "API $minSdk",
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
                    colors = acsTextFieldColors(),
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
                            text = { Text("API $sdk", color = AcsOnSurface) },
                            onClick = {
                                minSdk = sdk
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

            // Kotlin DSL toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Kotlin DSL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AcsOnSurface
                    )
                    Text(
                        "Use build.gradle.kts instead of build.gradle",
                        fontSize = 11.sp,
                        color = AcsOnSurfaceDim
                    )
                }
                Switch(
                    checked = useKts,
                    onCheckedChange = { useKts = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = AcsGold)
                )
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
                    onCreateProject(
                        projectName.ifBlank { "MyApp" },
                        packageName,
                        projectLocation,
                        language,
                        minSdk,
                        useKts
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AcsGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Create Project",
                    color = AcsBg,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ConfigField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Text(label, fontSize = 12.sp, color = AcsOnSurfaceVariant, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = AcsOnSurfaceDim) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = acsTextFieldColors(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun acsTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AcsGold,
    unfocusedBorderColor = AcsOutline,
    focusedTextColor = AcsOnSurface,
    unfocusedTextColor = AcsOnSurface,
    cursorColor = AcsGold,
    focusedContainerColor = AcsSurface2,
    unfocusedContainerColor = AcsSurface2
)