package dev.anvil.ade.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.GitBranch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.AcsGold
import dev.anvil.ade.ui.theme.AcsSurface4
import dev.anvil.ade.ui.theme.AcsTeal

/**
 * Home screen matching the reference: "Android Code Studio / Your Ideas, Anywhere"
 * with the ACS logo mark, tagline, and action list.
 */
@Composable
fun HomeScreen(
  onCreateProject: () -> Unit,
  onOpenProject: () -> Unit,
  onCloneGit: () -> Unit,
  onOpenTerminal: () -> Unit,
  onOpenPreferences: () -> Unit,
  onOpenIdeConfig: () -> Unit,
  onOpenDocs: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()
  val fadeIn by animateFloatAsState(targetValue = 1f, animationSpec = tween(400), label = "fade")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding()
      .verticalScroll(scrollState)
      .alpha(fadeIn),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(48.dp))

    // ---- ACS Logo Mark ----
    Box(
      modifier = Modifier
        .size(72.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(
          Brush.linearGradient(
            colors = listOf(AcsGold.copy(alpha = 0.25f), AcsTeal.copy(alpha = 0.15f))
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "<>",
        fontSize = 28.sp,
        fontWeight = FontWeight.Light,
        color = AcsGold,
        letterSpacing = (-2).sp
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // ---- Title ----
    Text(
      text = "Android Code Studio",
      style = MaterialTheme.typography.headlineLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center,
      letterSpacing = (-1.2).sp
    )

    Spacer(modifier = Modifier.height(4.dp))

    // ---- Tagline ----
    Text(
      text = "Your Ideas, Anywhere",
      style = MaterialTheme.typography.bodyLarge,
      color = AcsGold,
      fontWeight = FontWeight.Medium,
      textAlign = TextAlign.Center,
      letterSpacing = 0.5.sp
    )

    Spacer(modifier = Modifier.height(40.dp))

    // ---- "Get started" Section ----
    Text(
      text = "Get started",
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = 1.sp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 28.dp, vertical = 8.dp)
    )

    Spacer(modifier = Modifier.height(4.dp))

    // ---- Action List ----
    val actions = listOf(
      HomeAction("Create project", "Start your new awesome project!", Icons.Filled.Add, onCreateProject),
      HomeAction("Open existing project", "Import or browse for a project", Icons.Filled.FolderOpen, onOpenProject),
      HomeAction("Clone git repository", "Check out code from a remote repository", Icons.Outlined.GitBranch, onCloneGit),
      HomeAction("Terminal", "Open a terminal session", Icons.Filled.Terminal, onOpenTerminal),
      HomeAction("Preferences", "Configure IDE settings", Icons.Filled.Settings, onOpenPreferences),
      HomeAction("IDE Configurations", "IDE build system configurations", Icons.Filled.Tune, onOpenIdeConfig),
      HomeAction("Documentation", "Learn more about Android Code Studio", Icons.Filled.Description, onOpenDocs),
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
    ) {
      actions.forEachIndexed { idx, action ->
        HomeActionRow(
          action = action,
          isLast = idx == actions.lastIndex
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ---- Footer ----
    Text(
      text = "ACS",
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
      letterSpacing = 2.sp,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(16.dp))
  }
}

data class HomeAction(
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val onClick: () -> Unit
)

@Composable
private fun HomeActionRow(
  action: HomeAction,
  isLast: Boolean,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f))
      .clickable(onClick = action.onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(AcsSurface4.copy(alpha = 0.5f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = action.icon,
        contentDescription = null,
        tint = AcsGold,
        modifier = Modifier.size(18.dp)
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = action.title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1
      )
      Text(
        text = action.subtitle,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
    }
  }

  if (!isLast) {
    Spacer(modifier = Modifier.height(8.dp))
  }
}

