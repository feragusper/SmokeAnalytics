package com.feragusper.smokeanalytics.features.home.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import java.util.Date

sealed class HomeIntent : MVIIntent {
    data class TickTimeSinceLastCigarette(val lastCigarette: Smoke?) : HomeIntent()
    data class EditSmoke(val id: String, val date: Date) : HomeIntent()
    data class DeleteSmoke(val id: String) : HomeIntent()

    object AddSmoke : HomeIntent()
    object FetchSmokes : HomeIntent()
    object OnClickHistory : HomeIntent()
}
