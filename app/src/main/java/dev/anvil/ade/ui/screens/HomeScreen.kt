package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

@Composable
fun HomeScreen(
  onNewProject: () -> Unit,
  onOpenProject: () -> Unit,
  onCloneProject: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AcsBg)
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Spacer(Modifier.weight(0.6f))

    // Hero icon
    Box(
      modifier = Modifier
        .size(72.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(
          Brush.linearGradient(listOf(AcsGold, AcsTeal))
        ),
      contentAlignment = Alignment.Center
    ) {
      Text("⚒", fontSize = 32.sp)
    }

    Spacer(Modifier.height(24.dp))

    // Headline
    Text(
      "Your Ideas, Anywhere.",
      fontSize = 26.sp,
      fontWeight = FontWeight.SemiBold,
      color = AcsOnSurface,
      textAlign = TextAlign.Center,
      fontFamily = InterDisplay,
      letterSpacing = (-0.5).sp
    )

    Spacer(Modifier.height(8.dp))

    Text(
      "Build Android apps, Node.js services, and more —\non-device, with an AI pair programmer.",
      fontSize = 14.sp,
      color = AcsOnSurfaceVariant,
      textAlign = TextAlign.Center,
      lineHeight = 21.sp
    )

    Spacer(Modifier.height(36.dp))

    // Actions
    HomeActionButton("New Project", "Start fresh with a template", AcsGold, onNewProject)
    Spacer(Modifier.height(10.dp))
    HomeActionButton("Open Project", "Open an existing local project", AcsTeal, onOpenProject)
    Spacer(Modifier.height(10.dp))
    HomeActionButton("Clone Git Repo", "Clone from GitHub or GitLab", AcsOnSurfaceVariant, onCloneProject)

    Spacer(Modifier.weight(0.8f))
  }
}

@Composable
private fun HomeActionButton(
  title: String,
  subtitle: String,
  accent: androidx.compose.ui.graphics.Color,
  onClick: () -> Unit
) {
  Surface(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = AcsSurface2,
    tonalElevation = 0.dp
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(accent.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(accent)
        )
      }
      Spacer(Modifier.width(14.dp))
      Column {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AcsOnSurface)
        Text(subtitle, fontSize = 12.sp, color = AcsOnSurfaceDim)
      }
    }
  }
}