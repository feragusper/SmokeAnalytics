package com.feragusper.smokeanalytics.features.home.presentation.web

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlinx.datetime.Instant

// comments in English only where needed
sealed interface HomeIntent {
    data object FetchSmokes : HomeIntent
    data object RefreshFetchSmokes : HomeIntent
    data object AddSmoke : HomeIntent
    data class EditSmoke(val id: String, val date: Instant) : HomeIntent
    data class DeleteSmoke(val id: String) : HomeIntent
    data object OnClickHistory : HomeIntent
    data class TickTimeSinceLastCigarette(val lastCigarette: Smoke?) : HomeIntent
}