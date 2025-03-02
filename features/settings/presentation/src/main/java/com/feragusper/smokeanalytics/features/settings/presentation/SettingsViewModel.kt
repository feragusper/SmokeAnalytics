package com.feragusper.smokeanalytics.features.settings.presentation

import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsResult
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose.SettingsViewState
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator
import com.feragusper.smokeanalytics.features.settings.presentation.process.SettingsProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the Settings feature, responsible for processing user intents, interacting
 * with the domain layer, and updating the UI state.
 *
 * It extends [MVIViewModel] to implement the Model-View-Intent (MVI) architecture pattern.
 * This ViewModel handles settings screen-related logic and updates the UI state accordingly.
 *
 * @property processHolder Responsible for processing intents and invoking the corresponding actions.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val processHolder: SettingsProcessHolder,
) : MVIViewModel<SettingsIntent, SettingsViewState, SettingsResult, SettingsNavigator>(
    initialState = SettingsViewState()
) {

    /**
     * Navigator instance for handling navigation actions.
     */
    override lateinit var navigator: SettingsNavigator

    init {
        // Trigger initial intent to fetch user information.
        intents().trySend(SettingsIntent.FetchUser)
    }

    /**
     * Transforms [SettingsIntent] into a stream of [SettingsResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A Flow of [SettingsResult] representing the result of processing the intent.
     */
    override fun transformer(intent: SettingsIntent) = processHolder.processIntent(intent)

    /**
     * Reduces the previous [SettingsViewState] and a new [SettingsResult] to a new state.
     *
     * This function is responsible for creating the new state based on the current state and the result.
     *
     * @param previous The previous state of the UI.
     * @param result The result of processing the intent.
     * @return The new state of the UI.
     */
    override suspend fun reducer(
        previous: SettingsViewState,
        result: SettingsResult,
    ): SettingsViewState = when (result) {

        /**
         * Indicates that the user is logged in, updating the UI with the user's email.
         */
        is SettingsResult.UserLoggedIn -> previous.copy(
            displayLoading = false,
            currentEmail = result.email,
        )

        /**
         * Indicates that the user is logged out, clearing the user information from the UI.
         */
        SettingsResult.UserLoggedOut -> previous.copy(
            displayLoading = false,
            currentEmail = null,
        )

        /**
         * Indicates that the application is currently loading or processing data.
         */
        SettingsResult.Loading -> previous.copy(displayLoading = true)
    }
}
