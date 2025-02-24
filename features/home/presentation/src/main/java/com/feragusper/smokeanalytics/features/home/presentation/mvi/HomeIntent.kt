package com.feragusper.smokeanalytics.features.home.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import java.time.LocalDateTime

/**
 * Defines user intentions that trigger actions within the Home feature.
 */
sealed class HomeIntent : MVIIntent {
    data class TickTimeSinceLastCigarette(val lastCigarette: Smoke?) : HomeIntent()
    data class EditSmoke(val id: String, val date: LocalDateTime) : HomeIntent()
    data class DeleteSmoke(val id: String) : HomeIntent()

    /**
     * Represents an intent to add a new smoke entry.
     */
    object AddSmoke : HomeIntent()

    /**
     * Represents an intent to fetch smoke data and update the UI.
     */
    object FetchSmokes : HomeIntent()

    object RefreshFetchSmokes : HomeIntent()

    /**
     * Represents an intent to navigate to the smoke history screen.
     */
    object OnClickHistory : HomeIntent()
}
