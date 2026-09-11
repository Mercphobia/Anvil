package dev.anvil.ade.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.viewmodel.AnvilViewModel

/**
 * Welcome step — Blueprint style.
 * Wordmark ANVIL, amber CTA button, blueprintGrid background
 * via the parent OnboardingFlow Surface.
 */
@Composable
fun WelcomeStep(viewModel: AnvilViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Anvil logomark — monochrome, tinted with primary (amber)
        AnvilMark(
            modifier = Modifier.size(88.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "ANVIL",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 6.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Coding agent di genggaman",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(44.dp))
        Button(
            onClick = { viewModel.advanceWizard() },
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.fillMaxWidth(0.7f).height(48.dp)
        ) {
            Text("Get Started", style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "anvil.dev",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Monochrome anvil logomark — Canvas vector matching the launcher icon.
 * Body with horn, waist, base. Single color, flat fill.
 */
@Composable
fun AnvilMark(modifier: Modifier = Modifier, tint: androidx.compose.ui.graphics.Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sx = w / 100f
        val sy = h / 100f
        fun x(v: Float) = v * sx
        fun y(v: Float) = v * sy

        val body = Path().apply {
            moveTo(x(18f), y(30f))
            lineTo(x(58f), y(30f))
            lineTo(x(58f), y(24f))
            cubicTo(x(58f), y(20f), x(61f), y(18f), x(64f), y(18f))
            lineTo(x(86f), y(18f))
            cubicTo(x(90f), y(18f), x(91f), y(22f), x(88f), y(24f))
            lineTo(x(72f), y(38f))
            lineTo(x(22f), y(38f))
            cubicTo(x(19f), y(38f), x(17f), y(35f), x(18f), y(30f))
            close()
        }
        val waist = Path().apply {
            moveTo(x(40f), y(41f))
            lineTo(x(66f), y(41f))
            lineTo(x(61f), y(57f))
            lineTo(x(45f), y(57f))
            close()
        }
        val base = Path().apply {
            moveTo(x(34f), y(60f))
            lineTo(x(72f), y(60f))
            cubicTo(x(76f), y(60f), x(78f), y(62f), x(78f), y(65f))
            lineTo(x(78f), y(72f))
            lineTo(x(28f), y(72f))
            lineTo(x(28f), y(65f))
            cubicTo(x(28f), y(62f), x(30f), y(60f), x(34f), y(60f))
            close()
        }
        drawPath(body, tint, style = Fill)
        drawPath(waist, tint, style = Fill)
        drawPath(base, tint, style = Fill)
    }
}