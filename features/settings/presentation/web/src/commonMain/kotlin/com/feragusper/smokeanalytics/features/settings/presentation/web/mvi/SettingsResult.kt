package com.feragusper.smokeanalytics.features.settings.presentation.web.mvi

import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences

/**
 * Represents the results that can be returned from the Settings screen.
 */
sealed interface SettingsResult {
    /**
     * Represents the result of a loading operation.
     */
    data object Loading : SettingsResult

    /**
     * Represents the result of a successful fetch of the user.
     *
     * @property email The email of the user.
     */
    data class UserLoggedIn(
        val email: String?,
        val displayName: String?,
        val preferences: UserPreferences,
    ) : SettingsResult

    /**
     * Represents the result of a successful sign out.
     */
    data object UserLoggedOut : SettingsResult

    data object PreferencesSaved : SettingsResult

    /**
     * Represents the result of a generic error.
     */
    data class ErrorGeneric(val message: String = "Something went wrong") : SettingsResult
}
