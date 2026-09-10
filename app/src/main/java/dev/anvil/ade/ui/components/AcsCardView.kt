package dev.anvil.ade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.AcsGold
import dev.anvil.ade.ui.theme.AcsOutlineVariant
import dev.anvil.ade.ui.theme.AcsSurface2

/**
 * AOSP 17-style grouped card. Rows blend seamlessly inside a single elevated
 * container with rounded corners — no internal dividers, no per-row separators.
 *
 * Usage:
 *   AcsCardGroup(modifier = Modifier.padding(horizontal = 16.dp)) {
 *       AcsCardRow(icon = Icons.Filled.Settings, title = "General", subtitle = "App preferences")
 *       AcsCardRow(icon = Icons.Filled.Lock, title = "Privacy", onClick = { ... })
 *   }
 */

// ── Card group container ──────────────────────────────────────────────

@Composable
fun AcsCardGroup(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = AcsSurface2,
    borderColor: Color = AcsOutlineVariant,
    borderWidth: Dp = 0.5.dp,
    horizontalPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .background(containerColor, shape)
            .padding(horizontal = horizontalPadding),
        content = content
    )
}

// ── Card row (single item) ────────────────────────────────────────────

@Composable
fun AcsCardRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    accent: Color = AcsGold,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick)
            else Modifier
        )
        .padding(horizontal = 16.dp, vertical = 13.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading icon in rounded-square accent background
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        // Title + optional subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }

        // Trailing content or default chevron
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

// ── Convenience: AcsCardRow with a trailing Switch ────────────────────

@Composable
fun AcsCardSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color = AcsGold,
    modifier: Modifier = Modifier
) {
    AcsCardRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        accent = accent,
        modifier = modifier,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = accent)
            )
        }
    )
}

// ── Convenience: section header above a card ──────────────────────────

@Composable
fun AcsSectionLabel(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = AcsGold,
        letterSpacing = 1.sp,
        modifier = modifier.padding(start = 4.dp, top = 18.dp, bottom = 8.dp)
    )
}