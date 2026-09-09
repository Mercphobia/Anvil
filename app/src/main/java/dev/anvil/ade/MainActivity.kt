package dev.anvil.ade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dev.anvil.ade.ui.MainScaffold
import dev.anvil.ade.ui.theme.AnvilTheme
import dev.anvil.ade.viewmodel.AnvilViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: AnvilViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AnvilTheme {
        MainScaffold(viewModel = viewModel)
      }
    }
  }
}
