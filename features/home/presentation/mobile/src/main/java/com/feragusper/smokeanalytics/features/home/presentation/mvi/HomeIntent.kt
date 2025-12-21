package com.feragusper.smokeanalytics.features.home.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlinx.datetime.Instant

/**
 * Defines user intentions that trigger actions within the Home feature.
 *
 * This sealed class represents all possible user actions within the Home module,
 * allowing the ViewModel to handle them in a structured manner.
 */
sealed class HomeIntent : MVIIntent {

    /**
     * Represents an intent to update the time since the last cigarette.
     *
     * @property lastCigarette The last smoke event.
     */
    data class TickTimeSinceLastCigarette(val lastCigarette: Smoke?) : HomeIntent()

    /**
     * Represents an intent to edit an existing smoke entry.
     *
     * @property id The unique identifier of the smoke event to be edited.
     * @property date The new date and time for the smoke event.
     */
    data class EditSmoke(val id: String, val date: Instant) : HomeIntent()

    /**
     * Represents an intent to delete an existing smoke entry.
     *
     * @property id The unique identifier of the smoke event to be deleted.
     */
    data class DeleteSmoke(val id: String) : HomeIntent()

    /**
     * Represents an intent to add a new smoke entry.
     */
    data object AddSmoke : HomeIntent()

    /**
     * Represents an intent to fetch smoke data and update the UI.
     */
    data object FetchSmokes : HomeIntent()

    /**
     * Represents an intent to refresh the smoke data and update the UI.
     */
    data object RefreshFetchSmokes : HomeIntent()

    /**
     * Represents an intent to navigate to the smoke history screen.
     */
    data object OnClickHistory : HomeIntent()
}