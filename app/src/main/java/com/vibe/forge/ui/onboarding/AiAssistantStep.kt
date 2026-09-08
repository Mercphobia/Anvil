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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.vibe.forge.model.LlmProvider
import com.vibe.forge.viewmodel.VibeForgeViewModel

@Composable
fun AiAssistantStep(viewModel: VibeForgeViewModel) {
    val config by viewModel.providerConfig.collectAsState()
    var apiKeyInput by remember { mutableStateOf("") }

    val providers = listOf(
        LlmProvider.CLAUDE to "Claude Sonnet - best for agentic code",
        LlmProvider.OPENAI to "GPT-4o - direct OpenAI API",
        LlmProvider.OPENROUTER to "OpenRouter - any model, one key",
        LlmProvider.CUSTOM to "Custom - OpenAI-compatible endpoint"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "Select AI Assistant",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "The assistant writes code, fixes build errors, and designs UI. You can change this later in Settings.",
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
            providers.forEach { (provider, desc) ->
                val selected = config.provider == provider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectWizardProvider(provider) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = provider.displayName,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = {
                    apiKeyInput = it
                    viewModel.setWizardApiKey(it)
                },
                label = { Text("API Key (optional, can be set later)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
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
                onClick = { viewModel.advanceWizard() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Continue")
            }
        }
    }
}
