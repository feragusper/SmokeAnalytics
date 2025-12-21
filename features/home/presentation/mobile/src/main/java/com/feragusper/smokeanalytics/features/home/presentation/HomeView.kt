package com.feragusper.smokeanalytics.features.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Composable that displays the Home UI using the MVI architecture pattern.
 * It observes the [HomeViewModel] state and renders the UI accordingly.
 *
 * @param viewModel The [HomeViewModel] that manages the state and business logic for the Home feature.
 */
@Composable
fun HomeView(
    viewModel: HomeViewModel,
    onFabConfigChanged: (Boolean, (() -> Unit)?) -> Unit,
) {
    // Observe the ViewModel's state using collectAsState and remember to optimize recomposition.
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()

    // Render the UI based on the current state and send user intents to the ViewModel.
    viewState.Compose(
        onFabConfigChanged = onFabConfigChanged,
    ) { intent ->
        viewModel.intents().trySend(intent)
    }
}
