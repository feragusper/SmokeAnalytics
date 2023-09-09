package com.feragusper.smokeanalytics.features.profile.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

sealed class ProfileResult : MVIResult {
    object Loading : ProfileResult()
    data class UserLoggedIn(val displayName: String?) : ProfileResult()
    object UserLoggedOut : ProfileResult()
}
