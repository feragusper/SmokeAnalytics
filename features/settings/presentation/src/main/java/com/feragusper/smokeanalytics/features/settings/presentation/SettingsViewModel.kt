package com.feragusper.smokeanalytics.features.settings.presentation

import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsResult
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose.SettingsViewState
import com.feragusper.smokeanalytics.features.settings.presentation.process.SettingsProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the Settings feature, responsible for processing user intents, interacting
 * with the domain layer, and updating the UI state.
 *
 * @param processHolder Responsible for processing intents and invoking the corresponding actions.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val processHolder: SettingsProcessHolder,
) : MVIViewModel<SettingsIntent, SettingsViewState, SettingsResult, SettingsNavigator>(initialState = SettingsViewState()) {

    override lateinit var navigator: SettingsNavigator

    init {
        intents().trySend(SettingsIntent.FetchUser)
    }

    override suspend fun transformer(intent: SettingsIntent) = processHolder.processIntent(intent)

    override suspend fun reducer(
        previous: SettingsViewState,
        result: SettingsResult,
    ): SettingsViewState = when (result) {
        is SettingsResult.UserLoggedIn -> previous.copy(
            displayLoading = false,
            currentEmail = result.email,
        )

        SettingsResult.UserLoggedOut -> previous.copy(
            displayLoading = false,
            currentEmail = null,
        )

        SettingsResult.Loading -> previous.copy(displayLoading = true)
    }
}
