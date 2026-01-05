package com.feragusper.smokeanalytics.features.settings.presentation.web.mvi

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
    data class UserLoggedIn(val email: String?) : SettingsResult

    /**
     * Represents the result of a successful sign out.
     */
    data object UserLoggedOut : SettingsResult

    /**
     * Represents the result of a generic error.
     */
    data object ErrorGeneric : SettingsResult
}