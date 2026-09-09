package dev.anvil.ade.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.viewmodel.AnvilViewModel
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
  viewModel: AnvilViewModel,
  modifier: Modifier = Modifier
) {
  val pagerState = rememberPagerState(pageCount = { 3 })
  val coroutineScope = rememberCoroutineScope()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      // Top Navigation Bar: Brand tag + Skip button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Anvil",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
          )
        }

        TextButton(
          onClick = {
            viewModel.dismissWelcome()
            viewModel.setRoute("chat")
          },
          modifier = Modifier.testTag("skip_welcome_button")
        ) {
          Text(
            text = "Lewati",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Horizontal Pager Slides
      HorizontalPager(
        state = pagerState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) { page ->
        when (page) {
          0 -> IntroSlide()
          1 -> ModesInfoSlide()
          2 -> CapabilitiesSlide()
        }
      }

      // Bottom Control Bar: Indicators + Next/Start Button
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp)
      ) {
        // Dot Indicators
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(3) { index ->
            val isSelected = pagerState.currentPage == index
            val width by animateDpAsState(
              targetValue = if (isSelected) 24.dp else 8.dp,
              label = "indicator_width"
            )
            Box(
              modifier = Modifier
                .padding(horizontal = 3.dp)
                .height(8.dp)
                .width(width)
                .clip(CircleShape)
                .background(
                  if (isSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
          }
        }

        // Action Buttons Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (pagerState.currentPage > 0) {
            OutlinedButton(
              onClick = {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
              },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.height(48.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Sebelumnya",
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Kembali", fontSize = 13.sp)
            }
          } else {
            Spacer(modifier = Modifier.width(48.dp))
          }

          if (pagerState.currentPage < 2) {
            Button(
              onClick = {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
              },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .height(48.dp)
                .testTag("next_slide_button")
            ) {
              Text("Lanjut", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Lanjut",
                modifier = Modifier.size(16.dp)
              )
            }
          } else {
            Button(
              onClick = {
                viewModel.dismissWelcome()
                viewModel.setRoute("chat")
              },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
              ),
              modifier = Modifier
                .height(48.dp)
                .testTag("welcome_start_button")
            ) {
              Text("Mulai AI Chat", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Mulai",
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun IntroSlide() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Large Modern Forge Hero Icon
    Box(
      modifier = Modifier
        .size(80.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.AutoAwesome,
        contentDescription = "Forge Icon",
        modifier = Modifier.size(40.dp),
        tint = MaterialTheme.colorScheme.primary
      )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
      text = "Tempa Aplikasi Android Anda",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onBackground,
      letterSpacing = (-0.5).sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = "Lingkungan pengembangan Android & AOSP on-device bertenaga AI. Tulis kode, pratinjau antarmuka, dan kompilasi APK langsung di ponsel Anda.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      lineHeight = 22.sp
    )

    Spacer(modifier = Modifier.height(28.dp))

    // Sleek Highlight Pills
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      HighlightBadge(text = "On-Device Compiler")
      HighlightBadge(text = "Live Mockup")
      HighlightBadge(text = "AI-Powered")
    }
  }
}

@Composable
private fun ModesInfoSlide() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Dua Mode Kerja Otomatis",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Mode aktif ditentukan otomatis dari cara Anda membuka proyek - tidak perlu memilih manual.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Mode A Card
    SlideModeCard(
      title = "App Builder (APK)",
      description = "Aktif otomatis saat membuka proyek lokal. Membangun aplikasi Android Java mandiri dan mengompilasinya menjadi APK via aapt2, ecj, & d8.",
      badge = "LOKAL",
      icon = Icons.Filled.Build,
      isSelected = false,
      onClick = null
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Mode B Card
    SlideModeCard(
      title = "AOSP SystemUI Assist",
      description = "Aktif otomatis saat mengkloning repository Git. Merancang XML antarmuka SystemUI dengan live canvas mockup dan guard sanitasi resource.",
      badge = "GIT",
      icon = Icons.Filled.Palette,
      isSelected = false,
      onClick = null
    )
  }
}

@Composable
private fun CapabilitiesSlide() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Toolchain Lengkap",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Dilengkapi terminal bawaan, inspeksi pohon berkas, dan sistem Git.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    ToolFeatureRow(
      icon = Icons.Filled.Terminal,
      title = "Terminal On-Device",
      description = "Jalankan perintah toolchain aapt2, ecj, d8, git status, dan pm install secara instan."
    )

    Spacer(modifier = Modifier.height(12.dp))

    ToolFeatureRow(
      icon = Icons.Filled.Code,
      title = "Working Tree Sidebar",
      description = "Telusuri struktur proyek, lihat berkas yang dimodifikasi, dan navigasi editor langsung."
    )

    Spacer(modifier = Modifier.height(12.dp))

    ToolFeatureRow(
      icon = Icons.Filled.AutoAwesome,
      title = "Chat AI Interaktif",
      description = "Beri instruksi langsung dalam bahasa natural; AI akan menuliskan kode dan menjalankan aksi tool."
    )
  }
}

@Composable
private fun SlideModeCard(
  title: String,
  description: String,
  badge: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: (() -> Unit)?
) {
  Surface(
    onClick = onClick ?: {},
    shape = RoundedCornerShape(18.dp),
    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(
      if (isSelected) 2.dp else 1.dp,
      if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerHigh
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHighest
          ) {
            Text(
              text = badge,
              style = MaterialTheme.typography.labelSmall,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp,
          lineHeight = 16.sp
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      if (isSelected) {
        Box(
          modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = "Selected",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun ToolFeatureRow(
  icon: ImageVector,
  title: String,
  description: String
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp
        )
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp,
          lineHeight = 15.sp
        )
      }
    }
  }
}

@Composable
private fun HighlightBadge(text: String) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}
