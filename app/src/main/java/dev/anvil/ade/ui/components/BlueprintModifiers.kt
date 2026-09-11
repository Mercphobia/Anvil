package dev.anvil.ade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.anvil.ade.ui.theme.BlueprintTokens

// ═══════════════════════════════════════════════════════════
// Blueprint Signature Modifiers
// ═══════════════════════════════════════════════════════════

/**
 * Fine grid overlay reminiscent of engineering graph paper.
 * Applied to the background of ALL main screens (not inside
 * cards or dialogs).
 *
 * Usage: `Box(modifier = Modifier.fillMaxSize().blueprintGrid()) { ... }`
 */
fun Modifier.blueprintGrid(spacing: Int = 18): Modifier = this.drawBehind {
    val lineColor = BlueprintTokens.border
    val step = spacing.dp.toPx()
    var x = 0f
    while (x < size.width) {
        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), 1f)
        x += step
    }
    var y = 0f
    while (y < size.height) {
        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 1f)
        y += step
    }
}

/**
 * Corner bracket marks at all 4 corners — signature Blueprint
 * detail for cards, bottom sheets, and confirmation dialogs.
 *
 * RESERVE for: tool-call cards, bottom sheets, active file in
 * sidebar, confirmation dialogs. Do NOT use on every list row
 * or generic card — the effect turns into visual noise.
 *
 * Usage: `Box(modifier = Modifier.blueprintCorners()) { ... }`
 */
fun Modifier.blueprintCorners(
    size: Int = 8,
    color: Color = BlueprintTokens.accent
): Modifier = this.drawWithContent {
    drawContent()
    val s = size.dp.toPx()
    val stroke = 1.5.dp.toPx()
    val w = this.size.width
    val h = this.size.height

    // top-left
    drawLine(color, Offset(0f, 0f), Offset(s, 0f), stroke)
    drawLine(color, Offset(0f, 0f), Offset(0f, s), stroke)
    // top-right
    drawLine(color, Offset(w - s, 0f), Offset(w, 0f), stroke)
    drawLine(color, Offset(w, 0f), Offset(w, s), stroke)
    // bottom-left
    drawLine(color, Offset(0f, h - s), Offset(0f, h), stroke)
    drawLine(color, Offset(0f, h), Offset(s, h), stroke)
    // bottom-right
    drawLine(color, Offset(w - s, h), Offset(w, h), stroke)
    drawLine(color, Offset(w, h - s), Offset(w, h), stroke)
}

// ═══════════════════════════════════════════════════════════
// Ruler Tick (Blueprint drag handle / progress indicator)
// ═══════════════════════════════════════════════════════════

/**
 * Draws a "ruler tick" motif — short-long-short trio of horizontal
 * lines — used as drag handles in bottom sheets and as step
 * indicators in the Setup Wizard progress bar.
 *
 * Usage:
 * ```
 * // Drag handle
 * Box(Modifier.width(32.dp).height(4.dp).rulerTick())
 *
 * // Progress step (filled)
 * Box(Modifier.width(24.dp).height(4.dp).rulerTick(filled = true))
 * ```
 */
fun Modifier.rulerTick(
    filled: Boolean = false,
    color: Color = if (filled) BlueprintTokens.accent else BlueprintTokens.borderStrong
): Modifier = this.drawWithContent {
    drawContent()
    val midY = size.height / 2f
    val stroke = 1.5.dp.toPx()
    val dashLen = 4.dp.toPx()
    val gap = 3.dp.toPx()
    val totalUnit = dashLen + gap

    // Three dashes: short, long, short
    val dashes = listOf(
        1f,   // short
        2.5f, // long
        1f    // short
    )
    var cx = (size.width - dashes.sum() * totalUnit + gap) / 2f

    for (scale in dashes) {
        val len = dashLen * scale
        drawLine(color, Offset(cx, midY), Offset(cx + len, midY), stroke)
        cx += len + gap
    }
}

// ═══════════════════════════════════════════════════════════
// Pre-built composables (thin wrappers around modifiers)
// ═══════════════════════════════════════════════════════════

/**
 * Drag handle styled as a ruler tick — replacement for the
 * generic pill drag handle in bottom sheets.
 */
@Composable
fun RulerTickDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .rulerTick()
            .padding(vertical = 8.dp)
    )
}

/**
 * Card container with blueprint corner marks applied selectively.
 * Use this for important panels (tool-call cards, confirmation
 * dialogs, bottom sheet containers) — NOT for every list row.
 */
@Composable
fun BlueprintCard(
    modifier: Modifier = Modifier,
    cornerSize: Int = 8,
    padding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(BlueprintTokens.surface, RoundedCornerShape(12.dp))
            .border(1.dp, BlueprintTokens.border, RoundedCornerShape(12.dp))
            .blueprintCorners(size = cornerSize)
            .padding(padding)
    ) {
        content()
    }
}