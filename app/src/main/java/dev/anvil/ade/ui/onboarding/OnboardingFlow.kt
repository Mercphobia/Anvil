package dev.anvil.ade.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.anvil.ade.viewmodel.AnvilViewModel
import dev.anvil.ade.viewmodel.WizardStep

/**
 * Fullscreen onboarding wizard, replacing the old setup dialog.
 * Flow: Welcome -> Bootstrap (auto env setup) -> AI Assistant ->
 * Project Source (new/local/clone/settings) -> Template -> homepage.
 * Each step is a separate composable in this package; visual design of
 * the individual step bodies reuses the user's existing components.
 */
@Composable
fun OnboardingFlow(
    viewModel: AnvilViewModel,
    modifier: Modifier = Modifier
) {
    val step by viewModel.wizardStep.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Progress header (hidden on welcome)
            if (step != WizardStep.WELCOME) {
                WizardProgressHeader(step)
            }

            // Step body with fade transition
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "wizard_step"
            ) { current ->
                when (current) {
                    WizardStep.WELCOME -> WelcomeStep(viewModel)
                    WizardStep.BOOTSTRAP -> BootstrapStep(viewModel)
                    WizardStep.AI_ASSISTANT -> AiAssistantStep(viewModel)
                    WizardStep.PROJECT_SOURCE -> ProjectSourceStep(viewModel)
                    WizardStep.TEMPLATE -> TemplateStep(viewModel)
                }
            }
        }
    }
}

@Composable
private fun WizardProgressHeader(step: WizardStep) {
    val steps = listOf(
        WizardStep.BOOTSTRAP to "Environment",
        WizardStep.AI_ASSISTANT to "AI Assistant",
        WizardStep.PROJECT_SOURCE to "Project",
        WizardStep.TEMPLATE to "Template"
    )
    val currentIndex = steps.indexOfFirst { it.first == step }.coerceAtLeast(0)
    val progress = (currentIndex + 1).toFloat() / steps.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Step ${currentIndex + 1} of ${steps.size} • ${steps[currentIndex].second}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        // Vercel-style segmented progress: equal segments, active = ink, done = subtle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
        ) {
            repeat(steps.size) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(
                            if (i <= currentIndex) MaterialTheme.colorScheme.onBackground
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}
