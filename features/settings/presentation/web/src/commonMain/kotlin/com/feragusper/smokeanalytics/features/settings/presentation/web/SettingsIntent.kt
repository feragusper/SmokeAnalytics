package com.feragusper.smokeanalytics.features.settings.presentation.web

sealed interface SettingsIntent {
    data object FetchUser : SettingsIntent
    data object SignOut : SettingsIntent
}