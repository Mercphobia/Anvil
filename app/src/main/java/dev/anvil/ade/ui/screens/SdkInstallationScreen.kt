package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SdkInstallationScreen(onBack: () -> Unit, onDone: (String, String, String, Boolean, Boolean) -> Unit, modifier: Modifier = Modifier) {
  var sdk by remember { mutableStateOf("SDK 35") }
  var jdk by remember { mutableStateOf("JDK 17") }
  var ndk by remember { mutableStateOf("NDK 28") }
  var git by remember { mutableStateOf(true) }
  var ssh by remember { mutableStateOf(true) }
  Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface) }
      Text("SDK Installation", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Column(Modifier.weight(1f).padding(16.dp)) {
      Text("Install the development tools", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
      Spacer(Modifier.height(8.dp))
      Text(sdk, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(jdk, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(ndk, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Button(onClick = { onDone(sdk, jdk, ndk, git, ssh) }, modifier = Modifier.fillMaxWidth().padding(16.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
      Text("Done", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
    }
  }
}