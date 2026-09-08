package com.vibe.forge.ui.onboarding

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibe.forge.viewmodel.ProjectSource
import com.vibe.forge.viewmodel.VibeForgeViewModel

@Composable
fun TemplateStep(viewModel: VibeForgeViewModel) {
    val source by viewModel.wizardProjectSource.collectAsState()
    var selected by remember { mutableStateOf("empty_activity") }

    val templates = listOf(
        Triple("empty_activity", "Empty Activity", "Minimal single-Activity app (Java)"),
        Triple("no_activity", "No Activity", "Service-only / background project"),
        Triple("basic_views", "Basic Views", "Buttons, text fields, and a list"),
        Triple("aosp_overlay", "AOSP Overlay", "RRO overlay for SystemUI tweaks")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "Project Template",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = if (source == ProjectSource.NEW)
                "Pick a starting point for your new project."
            else
                "Templates are skipped for existing or cloned projects - finish to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (source == ProjectSource.NEW) {
                templates.forEach { (id, name, desc) ->
                    val isSelected = selected == id
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selected = id }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "Source: " + when (source) {
                        ProjectSource.LOCAL -> "local project"
                        ProjectSource.CLONE -> "GitHub clone"
                        else -> "new project"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.backWizard() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            Button(
                onClick = { viewModel.finishWizard(selected) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Finish")
            }
        }
    }
}
