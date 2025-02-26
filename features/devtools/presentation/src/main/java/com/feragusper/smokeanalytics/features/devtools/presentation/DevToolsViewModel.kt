package com.feragusper.smokeanalytics.features.devtools.presentation

import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsIntent
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.DevToolsResult
import com.feragusper.smokeanalytics.features.devtools.presentation.mvi.compose.DevToolsViewState
import com.feragusper.smokeanalytics.features.devtools.presentation.navigation.DevToolsNavigator
import com.feragusper.smokeanalytics.features.devtools.presentation.process.DevToolsProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for managing the state and processing user intents for the DevTools feature.
 * This ViewModel handles developer tools-related logic and updates the UI state accordingly.
 *
 * It extends [MVIViewModel] to implement the Model-View-Intent (MVI) architecture pattern.
 *
 * @property processHolder The process holder responsible for executing business logic.
 */
@HiltViewModel
class DevToolsViewModel @Inject constructor(
    private val processHolder: DevToolsProcessHolder,
) : MVIViewModel<DevToolsIntent, DevToolsViewState, DevToolsResult, DevToolsNavigator>(
    initialState = DevToolsViewState()
) {

    /**
     * Navigator instance for handling navigation actions.
     */
    override lateinit var navigator: DevToolsNavigator

    init {
        // Trigger initial intent to fetch user data.
        intents().trySend(DevToolsIntent.FetchUser)
    }

    /**
     * Transforms [DevToolsIntent] into a stream of [DevToolsResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A Flow of [DevToolsResult] representing the result of processing the intent.
     */
    override suspend fun transformer(intent: DevToolsIntent) = processHolder.processIntent(intent)

    /**
     * Reduces the previous [DevToolsViewState] and a new [DevToolsResult] to a new state.
     *
     * This function is responsible for creating the new state based on the current state and the result.
     *
     * @param previous The previous state of the UI.
     * @param result The result of processing the intent.
     * @return The new state of the UI.
     */
    override suspend fun reducer(
        previous: DevToolsViewState,
        result: DevToolsResult,
    ): DevToolsViewState = when (result) {
        is DevToolsResult.UserLoggedIn -> previous.copy(
            displayLoading = false,
            currentUser = result.user?.let {
                DevToolsViewState.User(
                    id = it.id,
                    email = it.email,
                )
            }
        )

        DevToolsResult.UserLoggedOut -> previous.copy(
            displayLoading = false,
            currentUser = null,
        )

        DevToolsResult.Loading -> previous.copy(displayLoading = true)
    }
}
