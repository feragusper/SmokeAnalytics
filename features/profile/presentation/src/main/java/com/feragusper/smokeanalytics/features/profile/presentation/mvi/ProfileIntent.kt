package com.feragusper.smokeanalytics.features.profile.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

sealed class ProfileIntent : MVIIntent {
    object FetchUser : ProfileIntent()
    object SignOut : ProfileIntent()
}
