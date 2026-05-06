package com.feragusper.smokeanalytics.features.goals.presentation.web.mvi

import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences

sealed interface GoalsResult {
    data object Loading : GoalsResult

    data class Loaded(
        val email: String?,
        val preferences: UserPreferences,
        val goalProgress: GoalProgress?,
    ) : GoalsResult

    data object LoggedOut : GoalsResult
    data object GoalSaved : GoalsResult
    data class ErrorGeneric(val message: String = "Something went wrong") : GoalsResult
}
