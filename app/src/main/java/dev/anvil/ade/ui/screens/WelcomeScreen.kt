package dev.anvil.ade.ui.screens

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
          1 -> ProjectTypesSlide()
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
