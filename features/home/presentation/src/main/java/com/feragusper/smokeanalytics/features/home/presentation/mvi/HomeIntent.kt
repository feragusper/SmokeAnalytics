package com.feragusper.smokeanalytics.features.home.presentation.mvi

import com.feragusper.smokeanalytics.features.home.domain.Smoke
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

sealed class HomeIntent : MVIIntent {
    data class TickTimeSinceLastCigarette(val lastCigarette: Smoke?) : HomeIntent()
    object AddSmoke : HomeIntent()
    object FetchSmokes : HomeIntent()
}
