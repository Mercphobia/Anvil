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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.anvil.ade.viewmodel.AnvilViewModel

/**
 * Welcome step - Vercel Geist style: monochrome anvil mark drawn in Canvas
 * (same geometry as the launcher icon), compressed display type, one dark
 * CTA button. No decorative color.
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
        AnvilMark(
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Anvil",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "The agentic development environment that lives in your pocket. Build Android APKs on-device, edit any language, ship from anywhere.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.85f)
        )
        Spacer(modifier = Modifier.height(44.dp))
        Button(
            onClick = { viewModel.advanceWizard() },
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier.fillMaxWidth(0.6f).height(44.dp)
        ) {
            Text("Get Started", style = MaterialTheme.typography.labelLarge)
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
 * Monochrome anvil logomark - Canvas vector mirroring the launcher icon
 * geometry: body with horn, waist, base. Single color, flat.
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

        // Body with horn (left point) and heel (right)
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
        // Waist
        val waist = Path().apply {
            moveTo(x(40f), y(41f))
            lineTo(x(66f), y(41f))
            lineTo(x(61f), y(57f))
            lineTo(x(45f), y(57f))
            close()
        }
        // Base
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
