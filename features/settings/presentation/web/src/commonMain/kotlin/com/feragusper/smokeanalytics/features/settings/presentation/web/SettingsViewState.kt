package com.feragusper.smokeanalytics.features.settings.presentation.web

import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences

/**
 * Represents the state of the Settings screen.
 *
 * @property displayLoading Whether the loading indicator should be displayed.
 * @property currentEmail The current email of the user.
 * @property errorMessage The error message to be displayed.
 */
data class SettingsViewState(
    val displayLoading: Boolean = false,
    val currentEmail: String? = null,
    val currentDisplayName: String? = null,
    val preferences: UserPreferences = UserPreferences(),
    val goalProgress: GoalProgress? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
