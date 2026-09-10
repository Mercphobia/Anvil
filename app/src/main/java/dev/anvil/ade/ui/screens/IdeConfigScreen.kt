package dev.anvil.ade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.components.AcsCardGroup
import dev.anvil.ade.ui.components.AcsCardRow
import dev.anvil.ade.ui.components.AcsCardSwitchRow
import dev.anvil.ade.ui.components.AcsSectionLabel
import dev.anvil.ade.ui.theme.*

/** NDK, CMake, and native toolchain configuration screen. */
@Composable
fun IdeConfigScreen(
  ndkVersion: String,
  cmakeVersion: String,
  useNdk: Boolean,
  useCcache: Boolean,
  onToggleNdk: (Boolean) -> Unit,
  onToggleCcache: (Boolean) -> Unit,
  onConfigureNdk: () -> Unit,
  onConfigureCmake: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AcsBg)
      .verticalScroll(rememberScrollState())
      .padding(bottom = 32.dp)
  ) {
    // Header
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(AcsSurface1)
        .padding(20.dp)
    ) {
      Column {
        Text(
          "IDE Configuration",
          fontSize = 22.sp,
          fontWeight = FontWeight.SemiBold,
          color = AcsOnSurface,
          fontFamily = InterDisplay
        )
        Spacer(Modifier.height(4.dp))
        Text(
          "Native toolchains and compiler flags",
          fontSize = 13.sp,
          color = AcsOnSurfaceVariant
        )
      }
    }

    Spacer(Modifier.height(16.dp))

    AcsSectionLabel("NATIVE TOOLCHAINS")

    AcsCardGroup {
      AcsCardSwitchRow(
        icon = Icons.Default.Build,
        title = "Android NDK",
        subtitle = if (useNdk) "Version $ndkVersion" else "Native compilation disabled",
        checked = useNdk,
        onCheckedChange = onToggleNdk
      )
      AcsCardRow(
        icon = Icons.Default.Settings,
        title = "NDK Path",
        subtitle = ndkVersion,
        onClick = onConfigureNdk
      )
    }

    Spacer(Modifier.height(8.dp))

    AcsCardGroup {
      AcsCardRow(
        icon = Icons.Default.Terminal,
        title = "CMake",
        subtitle = "Version $cmakeVersion",
        onClick = onConfigureCmake
      )
    }

    Spacer(Modifier.height(8.dp))

    AcsSectionLabel("COMPILER FLAGS")

    AcsCardGroup {
      AcsCardSwitchRow(
        icon = Icons.Default.Speed,
        title = "ccache",
        subtitle = "Accelerate recompilation",
        checked = useCcache,
        onCheckedChange = onToggleCcache
      )
    }

    Spacer(Modifier.height(8.dp))

    AcsSectionLabel("BUILD SYSTEM")

    AcsCardGroup {
      AcsCardRow(
        icon = Icons.Default.AccountTree,
        title = "Gradle JDK",
        subtitle = "JDK 21 (embedded)"
      )
      AcsCardRow(
        icon = Icons.Default.Memory,
        title = "Daemon Memory",
        subtitle = "2048 MB"
      )
    }
  }
}