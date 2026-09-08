package com.vibe.forge.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.vibe.forge.agent.tools.GitTools
import com.vibe.forge.ui.components.DiffViewer
import com.vibe.forge.vcs.GitCredentialStore
import com.vibe.forge.vcs.GitRepoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun GitScreen() {
    val context = LocalContext.current
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    var token by remember { mutableStateOf(GitCredentialStore.token(context)) }
    var remote by remember { mutableStateOf(GitCredentialStore.remote(context)) }
    var branch by remember { mutableStateOf(GitCredentialStore.branch(context)) }
    var message by remember { mutableStateOf("") }
    var diff by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(!GitCredentialStore.isConfigured(context)) }

    val repoDir = remember { File(context.filesDir, "workspace") }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Git",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { showSettings = !showSettings }) {
                Text("Config")
            }
            TextButton(onClick = {
                if (busy) return@TextButton
                busy = true
                scope.launch {
                    val tools = GitTools(GitRepoManager(repoDir, token))
                    diff = tools.getDiff()
                    busy = false
                }
            }) {
                Text("Diff")
            }
        }

        if (showSettings) {
            OutlinedTextField(
                value = remote,
                onValueChange = { remote = it },
                label = { Text("Remote URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = branch,
                onValueChange = { branch = it },
                label = { Text("Working branch (ai-mockup/...)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Git token") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = {
                    GitCredentialStore.save(context, token.trim(), remote.trim(), branch.trim())
                    showSettings = false
                    status = "saved"
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Save")
            }
        }

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Commit message") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            singleLine = true
        )

        Button(
            onClick = {
                if (busy || message.isBlank()) return@Button
                busy = true
                scope.launch {
                    val tools = GitTools(GitRepoManager(repoDir, token))
                    status = tools.commitAndPush(branch.trim(), message.trim())
                    busy = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = !busy && message.isNotBlank() && token.isNotBlank()
        ) {
            Text(if (busy) "Pushing..." else "Looks good, commit")
        }

        if (status.isNotEmpty()) {
            Text(
                status,
                color = if (status.startsWith("error")) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        DiffViewer(
            diff = diff,
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}
