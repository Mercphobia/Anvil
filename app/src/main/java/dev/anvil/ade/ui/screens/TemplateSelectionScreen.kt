package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Template(val id: String, val name: String, val desc: String, val icon: @Composable () -> Unit = {})

@Composable
fun TemplateSelectionScreen(onBack: () -> Unit, onSelectTemplate: (String) -> Unit, modifier: Modifier = Modifier) {
  Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface) }
      Text("Choose Template", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Column(Modifier.weight(1f).padding(16.dp)) {
      val templates = listOf(
        "no_activity" to "No Activity", "basic_activity" to "Basic Activity",
        "empty_activity" to "Empty Activity", "compose_activity" to "Compose Activity",
        "bottom_nav" to "Bottom Navigation", "nav_drawer" to "Navigation Drawer",
        "responsive" to "Responsive Activity", "game" to "Game Activity"
      )
      Column { templates.forEach { (id, name) ->
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerHigh)
          .clickable { onSelectTemplate(id) }.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically) {
          Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
      }}
    }
  }
}