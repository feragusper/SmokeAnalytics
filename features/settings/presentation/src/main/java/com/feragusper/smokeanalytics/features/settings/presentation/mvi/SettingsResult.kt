package com.feragusper.smokeanalytics.features.settings.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

/**
 * Represents the possible outcomes of processing [SettingsIntent] actions.
 */
sealed class SettingsResult : MVIResult {

    /**
     * Indicates that the application is currently processing an action.
     */
    object Loading : SettingsResult()

    /**
     * Indicates successful retrieval of the currently logged-in user's email.
     * @property email The email of the logged-in user.
     */
    data class UserLoggedIn(val email: String?) : SettingsResult()

    /**
     * Indicates that the user has successfully logged out.
     */
    object UserLoggedOut : SettingsResult()
}
