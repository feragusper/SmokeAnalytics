package com.feragusper.smokeanalytics.features.chatbot.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Composable that displays the Statistics UI using the MVI architecture pattern.
 * It observes the [ChatbotViewModel] state and renders the UI accordingly.
 *
 * @param viewModel The [ChatbotViewModel] that manages the state and business logic for the Statistics feature.
 */
@Composable
fun ChatbotView(viewModel: ChatbotViewModel) {
    // Observe the ViewModel's state using collectAsState and remember to optimize recomposition.
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()

    // Render the UI based on the current state and send user intents to the ViewModel.
    viewState.Compose { intent ->
        viewModel.intents().trySend(intent)
    }
}
