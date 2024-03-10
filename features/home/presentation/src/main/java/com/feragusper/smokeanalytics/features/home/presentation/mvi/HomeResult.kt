package com.feragusper.smokeanalytics.features.home.presentation.mvi

import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

sealed interface HomeResult : MVIResult {
    object Loading : HomeResult
    object NotLoggedIn : HomeResult
    object GoToLogin : HomeResult
    object GoToHistory : HomeResult
    object AddSmokeSuccess : HomeResult
    object EditSmokeSuccess : HomeResult
    object DeleteSmokeSuccess : HomeResult
    sealed interface Error : HomeResult {
        object Generic :
            Error

        object NotLoggedIn :
            Error
    }

    data class FetchSmokesSuccess(val smokeCountListResult: SmokeCountListResult) : HomeResult
    object FetchSmokesError : HomeResult
    data class UpdateTimeSinceLastCigarette(val timeSinceLastCigarette: Pair<Long, Long>) :
        HomeResult
}
