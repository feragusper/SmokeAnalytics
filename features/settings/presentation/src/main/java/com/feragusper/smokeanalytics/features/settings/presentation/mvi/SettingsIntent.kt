package com.feragusper.smokeanalytics.features.settings.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

/**
 * Defines user intentions that can trigger actions within the Settings feature.
 */
sealed class SettingsIntent : MVIIntent {

    /**
     * Represents an intent to fetch the current user's information.
     */
    object FetchUser : SettingsIntent()

    /**
     * Represents an intent to sign out the current user.
     */
    object SignOut : SettingsIntent()
}
