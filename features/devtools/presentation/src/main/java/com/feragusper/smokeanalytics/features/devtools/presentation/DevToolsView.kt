package com.feragusper.smokeanalytics.features.devtools.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Composable that displays the Developer Tools UI using the MVI architecture pattern.
 * It observes the [DevToolsViewModel] state and renders the UI accordingly.
 *
 * @param viewModel The [DevToolsViewModel] that manages the state and business logic for the DevTools feature.
 */
@Composable
fun DevToolsView(viewModel: DevToolsViewModel) {
    // Observe the ViewModel's state using collectAsState and remember to optimize recomposition.
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()

    // Render the UI based on the current state and send user intents to the ViewModel.
    viewState.Compose { intent ->
        viewModel.intents().trySend(intent)
    }
}
