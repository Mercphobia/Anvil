package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

data class TemplateItem(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val accent: Color
)

private val androidTemplates = listOf(
    TemplateItem("no_activity", "No Activity", "Empty project, no UI", Icons.Filled.Block, AcsOnSurfaceDim),
    TemplateItem("basic_activity", "Basic Activity", "Single activity with layout", Icons.Filled.Android, AcsFileIconKt),
    TemplateItem("empty_activity", "Empty Activity", "Minimal compose activity", Icons.Filled.CheckBoxOutlineBlank, AcsTeal),
    TemplateItem("compose_activity", "Compose Activity", "Jetpack Compose starter", Icons.Filled.AutoAwesome, AcsGold),
    TemplateItem("bottom_nav", "Bottom Navigation", "Material 3 bottom nav", Icons.Filled.ViewAgenda, AcsFileIconKt),
    TemplateItem("nav_drawer", "Navigation Drawer", "Drawer + top app bar", Icons.Filled.Menu, AcsGreen),
    TemplateItem("responsive", "Responsive Activity", "Adaptive layouts", Icons.Filled.TabletAndroid, AcsTeal),
    TemplateItem("game_activity", "Game Activity", "Game loop with SurfaceView", Icons.Filled.VideogameAsset, AcsRed)
)

@Composable
fun TemplateSelectionScreen(
    onBack: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AcsBg)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AcsSurface1)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = AcsOnSurface)
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Choose Template",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AcsOnSurface,
                    fontFamily = InterDisplay
                )
                Text(
                    "Select a starting point for your project",
                    fontSize = 12.sp,
                    color = AcsOnSurfaceVariant
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            items(androidTemplates) { template ->
                TemplateCard(
                    template = template,
                    onClick = { onSelectTemplate(template.id) }
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: TemplateItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AcsTemplateCard)
            .border(0.5.dp, AcsTemplateCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(template.accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                template.icon,
                contentDescription = null,
                tint = template.accent,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            template.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AcsOnSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            template.description,
            fontSize = 11.sp,
            color = AcsOnSurfaceDim,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            maxLines = 2
        )
    }
}