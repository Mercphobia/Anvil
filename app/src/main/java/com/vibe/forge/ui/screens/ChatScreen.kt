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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vibe.forge.ui.components.ChatBubble
import com.vibe.forge.ui.components.ChatMessage
import com.vibe.forge.ui.components.ChatRole

/**
 * Phase 1: static chat UI only. Agent loop arrives in Phase 2.
 */
@Composable
fun ChatScreen() {
    var input by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateOf(
            listOf(
                ChatMessage(ChatRole.AGENT, "Welcome to Vibe Forge. Describe the app you want to build, or the SystemUI change you want to design."),
                ChatMessage(ChatRole.USER, "Make the quick settings panel dark blue."),
                ChatMessage(ChatRole.AGENT, "Phase 1 placeholder - the agent loop goes live in Phase 2.")
            )
        )
    }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.value.size) {
        if (messages.value.isNotEmpty()) {
            listState.animateScrollToItem(messages.value.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.value) { msg ->
                ChatBubble(msg)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Describe your task...") },
                singleLine = true
            )
            Button(
                onClick = {
                    val text = input.trim()
                    if (text.isNotEmpty()) {
                        messages.value = messages.value + ChatMessage(ChatRole.USER, text)
                        input = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}
