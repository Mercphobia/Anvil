package dev.anvil.ade.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons; import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*; import androidx.compose.runtime.*
import androidx.compose.ui.Alignment; import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight; import androidx.compose.ui.unit.dp; import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

@Composable
fun IdeConfigScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
  Column(modifier.fillMaxSize().background(AcsBg)) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = AcsOnSurface) }
      Text("IDE Configurations", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AcsOnSurface)
    }
    HorizontalDivider(color = AcsOutlineVariant)
    Column(Modifier.weight(1f).padding(16.dp)) {
      Text("NDK Version", fontSize = 13.sp, color = AcsOnSurfaceVariant)
      Text("Not installed", fontSize = 14.sp, color = AcsOnSurface)
      Spacer(Modifier.height(12.dp))
      Text("CMake Version", fontSize = 13.sp, color = AcsOnSurfaceVariant)
      Text("Not installed", fontSize = 14.sp, color = AcsOnSurface)
      Spacer(Modifier.height(12.dp))
      Button(onClick = {}, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = AcsGold.copy(alpha = 0.15f))) {
        Icon(Icons.Filled.Download, null, tint = AcsGold); Spacer(Modifier.width(6.dp))
        Text("Download", color = AcsGold)
      }
    }
  }
}