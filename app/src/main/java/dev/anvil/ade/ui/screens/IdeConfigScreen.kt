package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IdeConfigScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
  Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface) }
      Text("IDE Configurations", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Column(Modifier.weight(1f).padding(16.dp)) {
      Text("NDK Version", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text("Not installed", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
      Spacer(Modifier.height(12.dp))
      Text("CMake Version", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text("Not installed", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
      Spacer(Modifier.height(12.dp))
      Button(onClick = {}, shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))) {
        Icon(Icons.Filled.Download, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(6.dp))
        Text("Download", color = MaterialTheme.colorScheme.primary)
      }
    }
  }
}