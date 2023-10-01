package com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

sealed class HomeResult : MVIResult {
    object Loading : HomeResult()
    object AddSmokeSuccess : HomeResult()
}
