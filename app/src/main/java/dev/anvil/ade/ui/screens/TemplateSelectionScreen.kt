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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

data class TemplateItem(
  val id: String,
  val name: String,
  val description: String,
  val icon: ImageVector,
  val accent: androidx.compose.ui.graphics.Color
)

val defaultTemplates = listOf(
  TemplateItem("android_compose", "Android Compose", "Jetpack Compose + M3", Icons.Default.Android, AcsFileIconKt),
  TemplateItem("android_views", "Android Views", "XML layouts + ViewBinding", Icons.Default.PhoneAndroid, AcsFileIconXml),
  TemplateItem("node_express", "Node.js Express", "REST API server", Icons.Default.Code, AcsGreen),
  TemplateItem("python_fastapi", "Python FastAPI", "Async web framework", Icons.Default.Code, AcsTeal),
  TemplateItem("rust_axum", "Rust Axum", "Tokio + Axum web", Icons.Default.Build, AcsRed),
  TemplateItem("go_gin", "Go Gin", "Gin HTTP framework", Icons.Default.Code, AcsTeal),
  TemplateItem("cpp_cmake", "C++ CMake", "Native CMake project", Icons.Default.Terminal, AcsOnSurfaceVariant),
  TemplateItem("generic", "Empty Project", "Blank canvas, any language", Icons.Default.InsertDriveFile, AcsOnSurfaceDim)
)

@Composable
fun TemplateSelectionScreen(
  onSelectTemplate: (TemplateItem) -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AcsBg)
      .padding(16.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) {
        Text(
          "Choose Template",
          fontSize = 20.sp,
          fontWeight = FontWeight.SemiBold,
          color = AcsOnSurface,
          fontFamily = InterDisplay
        )
        Spacer(Modifier.height(2.dp))
        Text("Select a starting point for your project", fontSize = 13.sp, color = AcsOnSurfaceVariant)
      }
      Text(
        "Cancel",
        fontSize = 14.sp,
        color = AcsOnSurfaceDim,
        modifier = Modifier.clickable(onClick = onCancel)
      )
    }

    Spacer(Modifier.height(20.dp))

    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f)
    ) {
      items(defaultTemplates) { template ->
        TemplateCard(template = template, onClick = { onSelectTemplate(template) })
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
      .clip(RoundedCornerShape(14.dp))
      .background(AcsTemplateCard)
      .border(0.5.dp, AcsTemplateCardBorder, RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(14.dp)
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(template.accent.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        template.icon,
        contentDescription = null,
        tint = template.accent,
        modifier = Modifier.size(20.dp)
      )
    }
    Spacer(Modifier.height(10.dp))
    Text(
      template.name,
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
      color = AcsOnSurface
    )
    Spacer(Modifier.height(3.dp))
    Text(
      template.description,
      fontSize = 11.sp,
      color = AcsOnSurfaceDim,
      lineHeight = 15.sp,
      maxLines = 2
    )
  }
}