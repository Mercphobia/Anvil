package com.vibe.forge.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibe.forge.agent.tools.BuildTools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun BuildScreen() {
    val context = LocalContext.current
    var log by remember { mutableStateOf("Build the current workspace project (MODE_A).\n") }
    var building by remember { mutableStateOf(false) }
    val scope = remember { CoroutineScope(Dispatchers.Main) }
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            "On-device Build",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Button(
            onClick = {
                if (building) return@Button
                building = true
                log = ""
                scope.launch {
                    val tools = BuildTools(
                        context.applicationContext,
                        File(context.filesDir, "workspace")
                    )
                    val result = tools.runBuild { line -> log += line + "\n" }
                    log += "\n" + result + "\n"
                    building = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            enabled = !building
        ) {
            Text(if (building) "Building..." else "Build + Install")
        }

        Text(
            log,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scroll)
        )
    }
}
