package dev.anvil.ade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.model.ProjectType
import dev.anvil.ade.model.ProviderConfig
import dev.anvil.ade.ui.theme.AcsGold

/**
 * Compact pill above the chat input bar showing the active project type
 * and LLM provider. Tapping opens the provider/model selector (settings dialog).
 *
 * Layout: rounded 20dp, AcsGold 12% alpha background, 10sp SemiBold text.
 * Example: "ANDROID · Anthropic Claude"
 */
@Composable
fun ProviderPill(
    projectType: ProjectType,
    providerConfig: ProviderConfig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AcsGold.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${projectType.name} \u00B7 ${providerConfig.provider.displayName}",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = AcsGold
        )
    }
}