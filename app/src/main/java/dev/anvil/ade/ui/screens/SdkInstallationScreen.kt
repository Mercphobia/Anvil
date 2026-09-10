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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.*

/** SDK installation and management screen. */
@Composable
fun SdkInstallationScreen(
  sdkVersion: String,
  buildToolsVersion: String,
  platformToolsInstalled: Boolean,
  buildToolsInstalled: Boolean,
  isInstalling: Boolean,
  installProgress: String,
  onInstallSdk: () -> Unit,
  onInstallBuildTools: () -> Unit,
  onInstallPlatformTools: () -> Unit,
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
          "SDK Manager",
          fontSize = 22.sp,
          fontWeight = FontWeight.SemiBold,
          color = AcsOnSurface,
          fontFamily = InterDisplay
        )
        Spacer(Modifier.height(4.dp))
        Text(
          "Android SDK ${sdkVersion}",
          fontSize = 13.sp,
          color = AcsOnSurfaceVariant,
          fontFamily = JetBrainsMono
        )
      }
    }

    Spacer(Modifier.height(16.dp))

    // SDK Platform
    SectionHeader("SDK Platform")
    SdkCard(
      name = "Android SDK Platform",
      version = sdkVersion,
      installed = platformToolsInstalled,
      isInstalling = isInstalling,
      onInstall = onInstallSdk
    )

    Spacer(Modifier.height(12.dp))

    // Build Tools
    SectionHeader("BUILD TOOLS")
    SdkCard(
      name = "Android SDK Build-Tools",
      version = buildToolsVersion,
      installed = buildToolsInstalled,
      isInstalling = isInstalling,
      onInstall = onInstallBuildTools
    )

    Spacer(Modifier.height(12.dp))

    // Platform Tools
    SectionHeader("PLATFORM TOOLS")
    SdkCard(
      name = "Android SDK Platform-Tools",
      version = "35.0.2",
      installed = platformToolsInstalled,
      isInstalling = isInstalling,
      onInstall = onInstallPlatformTools
    )

    // Progress indicator
    if (isInstalling) {
      Spacer(Modifier.height(16.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(AcsSurface2)
          .padding(16.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          CircularProgressIndicator(
            color = AcsGold,
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp
          )
          Spacer(Modifier.width(12.dp))
          Text(
            installProgress.ifBlank { "Installing..." },
            fontSize = 13.sp,
            color = AcsOnSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun SectionHeader(label: String) {
  Text(
    label,
    fontSize = 11.sp,
    fontWeight = FontWeight.SemiBold,
    color = AcsGold,
    letterSpacing = 1.sp,
    modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
  )
}

@Composable
private fun SdkCard(
  name: String,
  version: String,
  installed: Boolean,
  isInstalling: Boolean,
  onInstall: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(AcsSurface2)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        if (installed) Icons.Default.CheckCircle else Icons.Default.Download,
        contentDescription = null,
        tint = if (installed) AcsGreen else AcsGold,
        modifier = Modifier.size(22.dp)
      )
      Spacer(Modifier.width(12.dp))
      Column(Modifier.weight(1f)) {
        Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AcsOnSurface)
        Text("Version $version", fontSize = 11.sp, color = AcsOnSurfaceDim, fontFamily = JetBrainsMono)
      }
      if (installed) {
        Text("Installed", fontSize = 11.sp, color = AcsGreen)
      } else {
        TextButton(
          onClick = onInstall,
          enabled = !isInstalling
        ) {
          Text(
            "Install",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AcsGold
          )
        }
      }
    }
  }
}