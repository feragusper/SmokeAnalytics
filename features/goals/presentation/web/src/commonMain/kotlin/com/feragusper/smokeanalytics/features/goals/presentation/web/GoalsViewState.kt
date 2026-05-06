package com.feragusper.smokeanalytics.features.goals.presentation.web

import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences

data class GoalsViewState(
    val displayLoading: Boolean = false,
    val currentEmail: String? = null,
    val preferences: UserPreferences = UserPreferences(),
    val goalProgress: GoalProgress? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
