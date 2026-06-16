package com.feragusper.smokeanalytics.features.home.presentation.web.mvi

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlinx.datetime.Instant

/**
 * Represents the intents that can be sent to the Home screen.
 */
sealed interface HomeIntent {

    /**
     * Represents the intent to fetch smokes.
     */
    data object FetchSmokes : HomeIntent

    /**
     * Represents the intent to refresh the fetch smokes operation.
     */
    data object RefreshFetchSmokes : HomeIntent

    /**
     * Represents the intent to add a smoke.
     */
    data object AddSmoke : HomeIntent

    /**
     * Represents the intent to manually begin a new day.
     */
    data object StartNewDay : HomeIntent

    /**
     * Represents the intent to edit a smoke.
     *
     * @property id The ID of the smoke to edit.
     * @property date The new date of the smoke.
     */
    data class EditSmoke(val id: String, val date: Instant) : HomeIntent

    /**
     * Represents the intent to delete a smoke.
     *
     * @property id The ID of the smoke to delete.
     */
    data class DeleteSmoke(val id: String) : HomeIntent

    /**
     * Represents the intent to navigate to the history screen.
     */
    data object OnClickHistory : HomeIntent

    data object OnClickGoals : HomeIntent

    /**
     * Represents the intent to tick the time since last cigarette.
     *
     * @property lastCigarette The last cigarette smoked.
     */
    data class TickTimeSinceLastCigarette(val lastCigarette: Smoke?) : HomeIntent

    /**
     * Represents the intent to track a craving (the urge to smoke).
     */
    data object TrackCraving : HomeIntent

    /**
     * Resolves the active craving.
     *
     * @property craving The pending craving being resolved.
     * @property smoked True if the user smoked, false if they let the urge pass.
     */
    data class ResolveCraving(val craving: Craving, val smoked: Boolean) : HomeIntent

    /**
     * Dismisses the transient "it's already a good time" hint.
     */
    data object DismissCravingHint : HomeIntent

    /**
     * Dismisses the craving celebration.
     */
    data object DismissCravingCelebration : HomeIntent
}
