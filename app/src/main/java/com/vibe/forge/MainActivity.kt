package com.vibe.forge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.vibe.forge.ui.MainScaffold
import com.vibe.forge.ui.theme.VibeForgeTheme
import com.vibe.forge.viewmodel.VibeForgeViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: VibeForgeViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      VibeForgeTheme {
        MainScaffold(viewModel = viewModel)
      }
    }
  }
}
