package com.feragusper.smokeanalytics.features.settings.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

/**
 * Defines user intentions that can trigger actions within the Settings feature.
 *
 * This sealed class represents all possible user actions within the Settings module,
 * allowing the ViewModel to handle them in a structured manner.
 */
sealed class SettingsIntent : MVIIntent {

    /**
     * Represents an intent to fetch the current user's information.
     *
     * This is typically triggered when the Settings screen is opened to display user details.
     */
    object FetchUser : SettingsIntent()

    /**
     * Represents an intent to sign out the current user.
     *
     * This is triggered when the user chooses to log out from the application.
     */
    object SignOut : SettingsIntent()
}
