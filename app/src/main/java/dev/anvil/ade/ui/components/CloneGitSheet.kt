package dev.anvil.ade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
fun CloneGitSheet(
  onDismiss: () -> Unit,
  onClone: (repoUrl: String, branch: String, shallow: Boolean) -> Unit
) {
  var repoUrl by remember { mutableStateOf("") }
  var branch by remember { mutableStateOf("") }
  var shallowClone by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
      .background(MaterialTheme.colorScheme.surface)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 16.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        "Clone Repository",
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f)
      )
      IconButton(onClick = onDismiss) {
        Icon(Icons.Filled.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }

    Spacer(Modifier.height(20.dp))

    // Repository URL field
    Text(
      "Repository URL",
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      letterSpacing = 0.5.sp
    )
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
      value = repoUrl,
      onValueChange = { repoUrl = it },
      placeholder = { Text("https://github.com/user/repo.git", fontSize = 13.sp) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AcsGold,
        unfocusedBorderColor = AcsOutlineVariant,
        focusedContainerColor = AcsSurface3,
        unfocusedContainerColor = AcsSurface2,
        cursorColor = AcsGold
      )
    )

    Spacer(Modifier.height(16.dp))

    // Branch field (optional)
    Text(
      "Branch (optional)",
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      letterSpacing = 0.5.sp
    )
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
      value = branch,
      onValueChange = { branch = it },
      placeholder = { Text("main", fontSize = 13.sp) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AcsGold,
        unfocusedBorderColor = AcsOutlineVariant,
        focusedContainerColor = AcsSurface3,
        unfocusedContainerColor = AcsSurface2,
        cursorColor = AcsGold
      )
    )

    Spacer(Modifier.height(12.dp))

    // Shallow clone checkbox
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .clickable { shallowClone = !shallowClone }
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          "Shallow clone",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          "Only download the latest commit (--depth 1)",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Checkbox(
        checked = shallowClone,
        onCheckedChange = { shallowClone = it },
        colors = CheckboxDefaults.colors(checkedColor = AcsGold)
      )
    }

    Spacer(Modifier.height(24.dp))

    // Clone button
    Button(
      onClick = {
        if (repoUrl.isNotBlank()) {
          onClone(repoUrl, branch.ifBlank { "main" }, shallowClone)
        }
      },
      modifier = Modifier.fillMaxWidth().height(50.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = AcsGold),
      enabled = repoUrl.isNotBlank()
    ) {
      Text(
        "Clone",
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = AcsBg
      )
    }

    Spacer(Modifier.height(12.dp))
  }
}