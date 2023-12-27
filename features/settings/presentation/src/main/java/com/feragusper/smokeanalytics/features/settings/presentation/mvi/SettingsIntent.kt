package com.feragusper.smokeanalytics.features.settings.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

sealed class SettingsIntent : MVIIntent {
    object FetchUser : SettingsIntent()
    object SignOut : SettingsIntent()
}
