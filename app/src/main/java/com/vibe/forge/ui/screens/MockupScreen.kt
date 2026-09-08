package com.vibe.forge.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibe.forge.system.XmlResourceInterceptor
import com.vibe.forge.ui.components.MockupCanvas

/** Shared XML state so the agent's preview_mockup tool can push content here. */
object MockupState {
    val currentXml = mutableStateOf("")
    val lastValidation = mutableStateOf("")
}

@Composable
fun MockupScreen() {
    val xml by MockupState.currentXml
    val validation by MockupState.lastValidation
    var editXml by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = editXml,
                onValueChange = { editXml = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Paste AOSP layout XML...") },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                maxLines = 4
            )
            Button(
                onClick = {
                    val wellFormed = XmlResourceInterceptor.isWellFormed(editXml)
                    MockupState.lastValidation.value =
                        if (wellFormed) "XML OK" else "XML not well-formed"
                    if (wellFormed) MockupState.currentXml.value = editXml
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Preview")
            }
        }

        if (validation.isNotEmpty()) {
            Text(
                validation,
                color = if (validation == "XML OK") MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        MockupCanvas(
            xmlContent = xml,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp)
        )
    }
}
