package com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi

import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

sealed class HomeResult : MVIResult {
    object Loading : HomeResult()
    object AddSmokeSuccess : HomeResult()
    object AddSmokeError : HomeResult()
    data class FetchSmokesSuccess(val smokeCountListResult: SmokeCountListResult) : HomeResult()
    object FetchSmokesError : HomeResult()
}
