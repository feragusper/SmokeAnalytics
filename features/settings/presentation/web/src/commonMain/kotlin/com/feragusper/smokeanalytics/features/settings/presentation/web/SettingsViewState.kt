package com.feragusper.smokeanalytics.features.settings.presentation.web

data class SettingsViewState(
    val displayLoading: Boolean = false,
    val currentEmail: String? = null,
    val errorMessage: String? = null,
)