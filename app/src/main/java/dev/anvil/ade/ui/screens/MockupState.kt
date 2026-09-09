package dev.anvil.ade.ui.screens

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Shared state pushed by the agent's preview_mockup tool.
 * The MockupScreen composable observes this alongside the ViewModel state.
 */
object MockupState {
    val currentXml = MutableStateFlow("")
    val lastValidation = MutableStateFlow("")
}
