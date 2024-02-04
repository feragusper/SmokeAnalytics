package com.feragusper.smokeanalytics.features.devtools.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session

sealed class DevToolsResult : MVIResult {
    object Loading : DevToolsResult()
    data class UserLoggedIn(val user: Session.User?) : DevToolsResult()
    object UserLoggedOut : DevToolsResult()
}
