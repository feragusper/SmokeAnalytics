package com.feragusper.smokeanalytics.features.authentication.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Composable that displays the Authentication UI using the MVI architecture pattern.
 * It observes the [AuthenticationViewModel] state and renders the UI accordingly.
 *
 * @param viewModel The [AuthenticationViewModel] that manages the state and business logic for authentication.
 */
@Composable
fun AuthenticationView(viewModel: AuthenticationViewModel) {
    // Observe the ViewModel's state using collectAsState and remember to optimize recomposition.
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()

    // Render the UI based on the current state and send user intents to the ViewModel.
    viewState.Compose { intent ->
        viewModel.intents().trySend(intent)
    }
}
