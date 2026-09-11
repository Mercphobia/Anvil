package dev.anvil.ade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloneGitSheet(
  onDismiss: () -> Unit,
  onClone: (url: String, branch: String, token: String) -> Unit,
  modifier: Modifier = Modifier
) {
  var repoUrl by remember { mutableStateOf("") }
  var branch by remember { mutableStateOf("main") }
  var token by remember { mutableStateOf("") }
  var showToken by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .padding(20.dp)
  ) {
    // Handle bar
    Box(
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .width(36.dp).height(4.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(MaterialTheme.colorScheme.outline)
    )
    Spacer(Modifier.height(16.dp))

    // Header
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Filled.Link, contentDescription = null,
        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
      Spacer(Modifier.width(10.dp))
      Text("Clone Repository", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface)
      Spacer(Modifier.weight(1f))
      IconButton(onClick = onDismiss) {
        Icon(Icons.Filled.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
    Spacer(Modifier.height(20.dp))

    // Repository URL
    Text("Repository URL", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
      value = repoUrl, onValueChange = { repoUrl = it },
      placeholder = { Text("https://github.com/user/repo.git", color = MaterialTheme.colorScheme.onSurfaceVariant) },
      modifier = Modifier.fillMaxWidth(), singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = MaterialTheme.colorScheme.primary
      ),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
      shape = RoundedCornerShape(10.dp)
    )
    Spacer(Modifier.height(16.dp))

    // Branch
    Text("Branch", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
      value = branch, onValueChange = { branch = it },
      modifier = Modifier.fillMaxWidth(), singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = MaterialTheme.colorScheme.primary
      ),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
      shape = RoundedCornerShape(10.dp)
    )
    Spacer(Modifier.height(16.dp))

    // Token
    Text("Personal Access Token (optional)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
      value = token, onValueChange = { token = it },
      modifier = Modifier.fillMaxWidth(), singleLine = true,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = MaterialTheme.colorScheme.primary
      ),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(onDone = {
        if (repoUrl.isNotBlank()) onClone(repoUrl, branch, token)
      }),
      shape = RoundedCornerShape(10.dp),
      visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
      trailingIcon = {
        TextButton(onClick = { showToken = !showToken }) {
          Text(if (showToken) "Hide" else "Show", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    )
    Spacer(Modifier.height(24.dp))

    // Clone button
    Button(
      onClick = { onClone(repoUrl, branch, token) },
      enabled = repoUrl.isNotBlank(),
      modifier = Modifier.fillMaxWidth().height(48.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
      shape = RoundedCornerShape(10.dp)
    ) {
      Text("Clone Repository", color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(8.dp))
  }
}