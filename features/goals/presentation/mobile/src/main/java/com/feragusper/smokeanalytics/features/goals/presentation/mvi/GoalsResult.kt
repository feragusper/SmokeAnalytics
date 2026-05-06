package com.feragusper.smokeanalytics.features.goals.presentation.mvi

import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences

sealed class GoalsResult : MVIResult {
    data object Loading : GoalsResult()

    data class Loaded(
        val email: String?,
        val preferences: UserPreferences,
        val goalProgress: GoalProgress?,
    ) : GoalsResult()

    data object LoggedOut : GoalsResult()
    data object GoalSaved : GoalsResult()
    data class Error(val message: String) : GoalsResult()
}
