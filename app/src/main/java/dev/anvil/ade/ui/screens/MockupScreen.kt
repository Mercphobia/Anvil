package dev.anvil.ade.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.LocalThemeTokens
import dev.anvil.ade.viewmodel.AnvilViewModel
import dev.anvil.ade.ui.theme.GeistMono

@Composable
fun MockupScreen(
  viewModel: AnvilViewModel,
  modifier: Modifier = Modifier
) {
  val mockupXml by viewModel.mockupXml.collectAsState()
  val validationText by viewModel.mockupValidation.collectAsState()
  // Agent-pushed preview (from preview_mockup tool) takes precedence when set
  val agentXml by MockupState.currentXml.collectAsState()
  val agentValidation by MockupState.lastValidation.collectAsState()
  val effectiveXml = agentXml.ifBlank { mockupXml }
  val effectiveValidation = agentValidation.ifBlank { validationText }
  val tileStates by viewModel.tileStates.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Canvas, 1: XML Source
  var brightness by remember { mutableFloatStateOf(0.75f) }
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Minimalist Top Control Bar: Sleek Segmented Switch + Preset Selector
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
      ) {
        Row(modifier = Modifier.padding(3.dp)) {
          Surface(
            onClick = { selectedTab = 0 },
            shape = RoundedCornerShape(10.dp),
            color = if (selectedTab == 0) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
          ) {
            Text(
              text = "Live Canvas",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
              color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
          }

          Surface(
            onClick = { selectedTab = 1 },
            shape = RoundedCornerShape(10.dp),
            color = if (selectedTab == 1) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
          ) {
            Text(
              text = "XML Source",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
              color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
          }
        }
      }

      // Compact Presets
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
          onClick = { viewModel.loadMockupPreset("Quick Settings") },
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
          Text(
            text = "QS",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
          )
        }
        Surface(
          onClick = { viewModel.loadMockupPreset("Status Bar") },
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
          Text(
            text = "Status",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
          )
        }
        Surface(
          onClick = { viewModel.loadMockupPreset("Volume Panel") },
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
          Text(
            text = "Volume",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (selectedTab == 0) {
      // LIVE MOCKUP CANVAS (Simulating AOSP SystemUI with Expressive Monet Design)
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .testTag("mockup_canvas_card")
          .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
        ) {
          // System Status Bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "09:41",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Filled.Wifi, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
              Icon(Icons.Filled.SignalCellularAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
              Icon(Icons.Filled.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(18.dp), tint = LocalThemeTokens.current.ok)
            }
          }

          // Quick Settings Header: Date + User Avatar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Selasa, 8 September",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Quick Settings (Monet AOSP)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "VF",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }

          // Expressive Brightness Slider
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 16.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Filled.BrightnessMedium,
                contentDescription = "Brightness",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Slider(
                value = brightness,
                onValueChange = { brightness = it },
                modifier = Modifier.weight(1f)
              )
            }
          }

          // Monet Quick Settings Tiles (Interactive 2-column Grid)
          Text(
            text = "Ketuk tile untuk menguji state interaktif Monet:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          val tiles = listOf(
            Triple("Internet", Icons.Filled.Wifi, "Telkomsel 5G"),
            Triple("Bluetooth", Icons.Filled.Bluetooth, "Pixel Buds Pro"),
            Triple("Monet Dark", Icons.Filled.DarkMode, "Tema Aktif"),
            Triple("Flashlight", Icons.Filled.FlashlightOn, "Mati"),
            Triple("Hotspot", Icons.Filled.AirplanemodeActive, "Anvil-AP"),
            Triple("Night Light", Icons.Filled.Nightlight, "Otomatis")
          )

          for (i in tiles.indices step 2) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              val first = tiles[i]
              val firstActive = tileStates[first.first] ?: false
              QsExpressiveTile(
                name = first.first,
                subtitle = first.third,
                icon = first.second,
                isActive = firstActive,
                onClick = { viewModel.toggleTile(first.first) },
                modifier = Modifier.weight(1f)
              )

              if (i + 1 < tiles.size) {
                val second = tiles[i + 1]
                val secondActive = tileStates[second.first] ?: false
                QsExpressiveTile(
                  name = second.first,
                  subtitle = second.third,
                  icon = second.second,
                  isActive = secondActive,
                  onClick = { viewModel.toggleTile(second.first) },
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Lockscreen Media Player Mockup Card
          Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(14.dp))
                .background(
                  Brush.linearGradient(
                    colors = listOf(
                      MaterialTheme.colorScheme.primary,
                      MaterialTheme.colorScheme.secondary
                    )
                  )
                ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.PlayArrow,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "SystemUI Soundscape",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "AOSP Monet Audio Engine",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Next",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
              )
            }
          }
        }
      }
    } else {
      // XML SOURCE EDITOR & VALIDATOR
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Status bar for validation
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceContainer)
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.Security,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = effectiveValidation,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontFamily = GeistMono,
              modifier = Modifier.weight(1f)
            )
          }

          OutlinedTextField(
            value = effectiveXml,
            onValueChange = { viewModel.updateMockupXml(it) },
            modifier = Modifier
              .fillMaxSize()
              .testTag("mockup_xml_editor"),
            textStyle = TextStyle(
              fontFamily = GeistMono,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 17.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Color.Transparent,
              unfocusedBorderColor = Color.Transparent
            )
          )
        }
      }
    }
  }
}

@Composable
private fun QsExpressiveTile(
  name: String,
  subtitle: String,
  icon: ImageVector,
  isActive: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val containerColor = if (isActive) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.surfaceContainer
  }

  val contentColor = if (isActive) {
    MaterialTheme.colorScheme.onPrimary
  } else {
    MaterialTheme.colorScheme.onSurface
  }

  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(18.dp),
    color = containerColor,
    modifier = modifier.height(64.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(
            if (isActive) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surfaceContainerHigh
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = name,
          tint = contentColor,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = name,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = contentColor
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          fontSize = 9.sp,
          color = contentColor.copy(alpha = 0.75f),
          maxLines = 1
        )
      }
    }
  }
}
