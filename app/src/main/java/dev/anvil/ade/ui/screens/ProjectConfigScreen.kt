package dev.anvil.ade.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

@Composable
fun ProjectConfigScreen(
  onBack: () -> Unit,
  onCreateProject: (String, String, String, String, String, Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var name by remember { mutableStateOf("MyApp") }
  var pkg by remember { mutableStateOf("com.example.myapp") }
  var loc by remember { mutableStateOf("/storage/emulated/0/AndroidIDEProjects") }
  var lang by remember { mutableStateOf("Java") }
  var minSdk by remember { mutableStateOf("API 21") }
  var useKts by remember { mutableStateOf(true) }
  Column(modifier.fillMaxSize().background(AcsBg)) {
    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = AcsOnSurface) }
      Text("Project Configuration", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AcsOnSurface)
    }
    HorizontalDivider(color = AcsOutlineVariant)
    Column(Modifier.weight(1f).padding(16.dp)) {
      OutlinedTextField(name, { name = it }, label = { Text("Project name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AcsGold))
      Spacer(Modifier.height(12.dp))
      OutlinedTextField(pkg, { pkg = it }, label = { Text("Package name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AcsGold))
      Spacer(Modifier.height(12.dp))
      OutlinedTextField(loc, { loc = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AcsGold))
    }
    Button(onClick = { onCreateProject(name, pkg, loc, lang, minSdk, useKts) }, modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = AcsGold)) {
      Text("Create Project", color = AcsBg, fontWeight = FontWeight.SemiBold)
    }
  }
}