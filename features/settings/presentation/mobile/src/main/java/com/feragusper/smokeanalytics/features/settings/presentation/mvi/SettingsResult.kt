package com.feragusper.smokeanalytics.features.settings.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

/**
 * Represents the possible outcomes of processing [SettingsIntent] actions.
 *
 * This sealed class defines all possible states the Settings module can be in,
 * based on the result of processing a user intent.
 */
sealed class SettingsResult : MVIResult {

    /**
     * Indicates that the application is currently processing an action.
     * This is typically used to show a loading indicator.
     */
    object Loading : SettingsResult()

    /**
     * Indicates successful retrieval of the currently logged-in user's email.
     *
     * This result is used to update the UI with the user's email information.
     *
     * @property email The email of the logged-in user, or null if not available.
     */
    data class UserLoggedIn(val email: String?) : SettingsResult()

    /**
     * Indicates that the user has successfully logged out.
     *
     * This result is used to update the UI state to reflect that no user is currently logged in.
     */
    object UserLoggedOut : SettingsResult()
}
