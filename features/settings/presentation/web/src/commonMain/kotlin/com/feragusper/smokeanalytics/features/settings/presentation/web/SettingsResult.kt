package com.feragusper.smokeanalytics.features.settings.presentation.web

sealed interface SettingsResult {
    data object Loading : SettingsResult
    data class UserLoggedIn(val email: String?) : SettingsResult
    data object UserLoggedOut : SettingsResult
    data object ErrorGeneric : SettingsResult
}