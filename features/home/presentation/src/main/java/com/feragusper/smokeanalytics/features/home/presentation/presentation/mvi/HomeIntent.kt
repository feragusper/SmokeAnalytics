package com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

sealed class HomeIntent : MVIIntent {
    object AddSmoke : HomeIntent()
}
