package com.feragusper.smokeanalytics.features.settings.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

sealed class SettingsResult : MVIResult {
    object Loading : SettingsResult()
    data class UserLoggedIn(val email: String?) : SettingsResult()
    object UserLoggedOut : SettingsResult()
}
