package dev.anvil.ade.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.GeistMono
import dev.anvil.ade.viewmodel.AnvilViewModel
import dev.anvil.ade.viewmodel.WizardStep

/**
 * Fullscreen onboarding wizard — Blueprint design system.
 * Flow: Welcome → Provider Setup → Workspace Permission → Ready.
 * Progress indicator: ruler-tick motif (short-long-short dashes),
 * consistent with the engineering blueprint visual language.
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

            // Ruler-tick progress header (hidden on welcome)
            if (step != WizardStep.WELCOME) {
                BlueprintWizardProgress(step)
            }

            // Step body with fade transition
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.fillMaxWidth().weight(1f),
                label = "wizard_step"
            ) { current ->
                when (current) {
                    WizardStep.WELCOME -> WelcomeStep(viewModel)
                    WizardStep.PROVIDER_SETUP -> AiAssistantStep(viewModel)
                    WizardStep.WORKSPACE_PERMISSION -> WorkspacePermissionStep(viewModel)
                    WizardStep.READY -> ReadyStep(viewModel)
                }
            }
        }
    }
}

/**
 * Ruler-tick progress indicator — 5 dashes for 5 steps.
 * Completed steps fill with amber (primary), upcoming steps
 * remain outline. The motif is short-long-short-long-short,
 * mirroring the engineering ruler visual.
 */
@Composable
private fun BlueprintWizardProgress(step: WizardStep) {
    val steps = listOf(
            WizardStep.PROVIDER_SETUP to "Provider",
            WizardStep.WORKSPACE_PERMISSION to "Permission",
            WizardStep.READY to "Ready"
        )
    val currentIndex = steps.indexOfFirst { it.first == step }.coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "STEP ${currentIndex + 1}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = GeistMono,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Text(
                text = steps[currentIndex].second,
                fontSize = 10.sp,
                fontFamily = GeistMono,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Ruler-tick dashes: short-long-short-long-short (5 steps)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            repeat(steps.size) { i ->
                val filled = i <= currentIndex
                val weight = if (i % 2 == 0) 0.6f else 1.4f // short, long, short, long, short
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .height(3.dp)
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}