package com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi

import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

sealed interface HomeResult : MVIResult {
    object Loading : HomeResult
    object NotLoggedIn : HomeResult
    object GoToLogin : HomeResult
    object AddSmokeSuccess : HomeResult
    sealed interface AddSmokeError : HomeResult {
        object Generic : AddSmokeError
        object NotLoggedIn : AddSmokeError
    }

    data class FetchSmokesSuccess(val smokeCountListResult: SmokeCountListResult) : HomeResult
    object FetchSmokesError : HomeResult
}
