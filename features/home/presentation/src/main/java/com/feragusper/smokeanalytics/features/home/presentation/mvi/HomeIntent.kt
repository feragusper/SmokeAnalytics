package com.feragusper.smokeanalytics.features.home.presentation.mvi

import com.feragusper.smokeanalytics.features.home.domain.Smoke
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import java.util.Date

sealed class HomeIntent : MVIIntent {
    data class TickTimeSinceLastCigarette(val lastCigarette: Smoke?) : HomeIntent()
    data class EditSmoke(val id: String, val date: Date) : HomeIntent()

    object AddSmoke : HomeIntent()
    object FetchSmokes : HomeIntent()
}
