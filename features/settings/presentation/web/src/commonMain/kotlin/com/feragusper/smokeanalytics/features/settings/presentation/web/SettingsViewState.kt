package com.feragusper.smokeanalytics.features.settings.presentation.web

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
    val errorMessage: String? = null,
)