package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

data class ProjectTemplate(
  val id: String,
  val name: String,
  val description: String,
  val icon: ImageVector,
  val accent: androidx.compose.ui.graphics.Color = AcsGold
)

val availableTemplates = listOf(
  ProjectTemplate("no_activity", "No Activity", "Empty project with no activity", Icons.Filled.Code),
  ProjectTemplate("basic_activity", "Basic Activity", "Single activity with basic setup", Icons.Filled.PlayArrow),
  ProjectTemplate("empty_activity", "Empty Activity", "Minimal empty compose activity", Icons.Filled.CheckBoxOutlineBlank),
  ProjectTemplate("compose_activity", "Compose Activity", "Jetpack Compose ready activity", Icons.Filled.AutoAwesome),
  ProjectTemplate("bottom_nav", "Bottom Navigation", "Navigation with bottom bar", Icons.Filled.ViewAgenda),
  ProjectTemplate("nav_drawer", "Navigation Drawer", "Side drawer navigation", Icons.Filled.Menu),
  ProjectTemplate("responsive", "Responsive Activity", "Adapts to screen size", Icons.Filled.Devices),
  ProjectTemplate("game", "Game Activity", "Basic game loop template", Icons.Filled.SportsEsports),
)

@Composable
fun TemplateSelectionScreen(
  onBack: () -> Unit,
  onSelectTemplate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Text("Choose Template", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier.padding(12.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(bottom = 24.dp)
    ) {
      items(availableTemplates.size) { idx ->
        val tmpl = availableTemplates[idx]
        TemplateCard(tmpl) { onSelectTemplate(tmpl.id) }
      }
    }
  }
}

@Composable
private fun TemplateCard(tmpl: ProjectTemplate, onClick: () -> Unit) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(14.dp))
      .background(AcsTemplateCard)
      .clickable(onClick = onClick)
      .padding(16.dp)
      .fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
        .background(Brush.linearGradient(listOf(tmpl.accent.copy(alpha = 0.2f), AcsTeal.copy(alpha = 0.1f)))),
      contentAlignment = Alignment.Center
    ) {
      Icon(tmpl.icon, null, tint = tmpl.accent, modifier = Modifier.size(22.dp))
    }
    Spacer(Modifier.height(10.dp))
    Text(tmpl.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
    Spacer(Modifier.height(4.dp))
    Text(tmpl.description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 14.sp)
  }
}