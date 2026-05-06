package com.feragusper.smokeanalytics.features.goals.presentation

import com.feragusper.smokeanalytics.features.goals.presentation.mvi.GoalsIntent
import com.feragusper.smokeanalytics.features.goals.presentation.mvi.GoalsResult
import com.feragusper.smokeanalytics.features.goals.presentation.mvi.compose.GoalsViewState
import com.feragusper.smokeanalytics.features.goals.presentation.navigation.GoalsNavigator
import com.feragusper.smokeanalytics.features.goals.presentation.process.GoalsProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val processHolder: GoalsProcessHolder,
) : MVIViewModel<GoalsIntent, GoalsViewState, GoalsResult, GoalsNavigator>(
    initialState = GoalsViewState(displayLoading = true),
) {

    override lateinit var navigator: GoalsNavigator

    init {
        intents().trySend(GoalsIntent.FetchGoals)
    }

    override fun transformer(intent: GoalsIntent) = processHolder.processIntent(intent)

    override fun reducer(
        previous: GoalsViewState,
        result: GoalsResult,
    ): GoalsViewState = when (result) {
        GoalsResult.Loading -> previous.copy(displayLoading = true, infoMessage = null, errorMessage = null)

        is GoalsResult.Loaded -> previous.copy(
            displayLoading = false,
            currentEmail = result.email,
            preferences = result.preferences,
            goalProgress = result.goalProgress,
            infoMessage = null,
            errorMessage = null,
        )

        GoalsResult.LoggedOut -> previous.copy(
            displayLoading = false,
            currentEmail = null,
            preferences = UserPreferences(),
            goalProgress = null,
            infoMessage = null,
            errorMessage = null,
        )

        GoalsResult.GoalSaved -> previous.copy(
            displayLoading = false,
            infoMessage = null,
            errorMessage = null,
        )

        is GoalsResult.Error -> previous.copy(
            displayLoading = false,
            errorMessage = result.message,
        )
    }
}
