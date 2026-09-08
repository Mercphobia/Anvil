package com.vibe.forge.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.vibe.forge.editor.SoraEditorWrapper
import com.vibe.forge.ui.components.FileTree
import com.vibe.forge.workspace.FileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProjectScreen() {
    val context = LocalContext.current
    val repo = remember { FileRepository(File(context.filesDir, "workspace")) }
    val tree by repo.tree.collectAsState()
    val openFile by repo.openFile.collectAsState()
    val openContent by repo.openFileContent.collectAsState()
    val error by repo.error.collectAsState()
    var localText by remember(openFile) { mutableStateOf(openContent) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    LaunchedEffect(Unit) { repo.refresh() }
    LaunchedEffect(openContent) { localText = openContent }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                openFile?.name ?: "Workspace",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (openFile != null) {
                Button(onClick = { scope.launch { repo.save(localText) } }) {
                    Text("Save")
                }
                TextButton(onClick = {
                    scope.launch {
                        repo.open(File("/dev/null"))
                        repo.refresh()
                    }
                }) {
                    Text("Close")
                }
            } else {
                TextButton(onClick = { scope.launch { repo.refresh() } }) {
                    Text("Refresh")
                }
            }
        }

        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (openFile == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                tree?.children?.forEach { node ->
                    FileTree(node) { clicked ->
                        scope.launch { repo.open(clicked.file) }
                    }
                } ?: Text(
                    "Empty workspace",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            SoraEditorWrapper(
                text = localText,
                onTextChanged = { localText = it },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }
}
