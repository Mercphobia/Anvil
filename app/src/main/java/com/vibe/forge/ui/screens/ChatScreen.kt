package com.vibe.forge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibe.forge.agent.AgentSession
import com.vibe.forge.agent.ProviderConfigStore
import com.vibe.forge.ui.components.ChatBubble
import com.vibe.forge.ui.components.ChatMessage
import com.vibe.forge.ui.components.ChatRole
import com.vibe.forge.ui.components.ProviderSettingsDialog
import java.io.File

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    var config by remember { mutableStateOf(ProviderConfigStore.load(context)) }
    var showSettings by remember { mutableStateOf(false) }
    var session by remember(config) {
        mutableStateOf(
            AgentSession(
                config = config,
                workspaceRoot = File(context.filesDir, "workspace"),
                appContext = context.applicationContext
            )
        )
    }
    val steps by session.steps.collectAsState()
    val busy by session.busy.collectAsState()
    val pendingMemory by session.pendingMemoryEntry.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(steps.size) {
        if (steps.isNotEmpty()) listState.animateScrollToItem(steps.size - 1)
    }

    if (showSettings) {
        ProviderSettingsDialog(
            initial = config,
            onSave = { newConfig ->
                ProviderConfigStore.save(context, newConfig)
                config = newConfig
                showSettings = false
            },
            onDismiss = { showSettings = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Top bar: mode + provider info + settings
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = session.mode == AgentSession.Mode.MODE_A,
                onClick = { session.mode = AgentSession.Mode.MODE_A },
                label = { Text("App Builder") }
            )
            FilterChip(
                selected = session.mode == AgentSession.Mode.MODE_B,
                onClick = { session.mode = AgentSession.Mode.MODE_B },
                label = { Text("AOSP Assist") }
            )
            TextButton(onClick = { showSettings = true }) {
                Text(config.provider.displayName.split(" ").first())
            }
        }

        if (session.lastLoadedSkills.isNotEmpty()) {
            Text(
                "skills: " + session.lastLoadedSkills.joinToString(", "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(steps) { step ->
                when (step.kind) {
                    AgentSession.Step.Kind.USER ->
                        ChatBubble(ChatMessage(ChatRole.USER, step.text))
                    AgentSession.Step.Kind.AGENT_TEXT ->
                        ChatBubble(ChatMessage(ChatRole.AGENT, step.text))
                    AgentSession.Step.Kind.TOOL_CALL ->
                        StepLine("> tool: " + step.text, MaterialTheme.colorScheme.primary)
                    AgentSession.Step.Kind.TOOL_RESULT ->
                        StepLine(step.text, MaterialTheme.colorScheme.onSurfaceVariant)
                    AgentSession.Step.Kind.ERROR ->
                        StepLine("error: " + step.text, MaterialTheme.colorScheme.error)
                    AgentSession.Step.Kind.INFO ->
                        StepLine(step.text, MaterialTheme.colorScheme.secondary)
                }
            }
        }

        pendingMemory?.let { entry ->
            androidx.compose.material3.Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Save to project memory?",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        entry.take(120),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { session.dismissMemoryEntry() }) {
                            Text("Dismiss")
                        }
                        Button(
                            onClick = { session.confirmMemoryEntry() },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (config.isUsable()) "Describe your task..."
                        else "Set API key first (tap provider name)"
                    )
                },
                singleLine = true,
                enabled = !busy
            )
            Button(
                onClick = {
                    val text = input.trim()
                    if (text.isNotEmpty()) {
                        session.send(text)
                        input = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp),
                enabled = !busy && config.isUsable()
            ) {
                Text(if (busy) "..." else "Send")
            }
        }
    }
}

@Composable
private fun StepLine(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text,
        color = color,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        modifier = Modifier.padding(start = 8.dp)
    )
}
