package dev.anvil.ade.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.GeistMono

/**
 * Blueprint splash / loading screen.
 * Wordmark ANVIL + ruler-tick progress bar that animates
 * infinitely during long operations (build, bootstrap, etc.).
 */
@Composable
fun BlueprintSplash(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_progress")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress_bar"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ANVIL",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = GeistMono,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 10.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Ruler-tick loading bar: 5 dashes (short-long-short-long-short)
        // Only the portion up to 'progress' is filled with amber
        Row(
            modifier = Modifier.width(80.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(5) { i ->
                val dashProgress = (progress * 5f - i).coerceIn(0f, 1f)
                val weight = if (i % 2 == 0) 0.6f else 1.4f
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                ) {
                    // Filled portion (amber)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(dashProgress)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        if (message != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 11.sp,
                fontFamily = GeistMono,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }
    }
}