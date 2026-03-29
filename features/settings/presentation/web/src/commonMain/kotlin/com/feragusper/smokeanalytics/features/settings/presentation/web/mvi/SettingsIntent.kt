package com.feragusper.smokeanalytics.features.settings.presentation.web.mvi

import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences

/**
 * Represents the intents that can be sent to the Settings screen.
 */
sealed interface SettingsIntent {

    /**
     * Represents the intent to fetch the user.
     */
    data object FetchUser : SettingsIntent

    data class UpdatePreferences(
        val preferences: UserPreferences,
    ) : SettingsIntent

    /**
     * Represents the intent to sign out.
     */
    data object SignOut : SettingsIntent
}
