package com.feragusper.smokeanalytics.features.settings.presentation.web.mvi

/**
 * Represents the intents that can be sent to the Settings screen.
 */
sealed interface SettingsIntent {

    /**
     * Represents the intent to fetch the user.
     */
    data object FetchUser : SettingsIntent

    data class UpdatePreferences(
        val packPrice: Double,
        val cigarettesPerPack: Int,
        val dayStartHour: Int,
        val locationTrackingEnabled: Boolean,
    ) : SettingsIntent

    /**
     * Represents the intent to sign out.
     */
    data object SignOut : SettingsIntent
}
