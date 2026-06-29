package com.feragusper.smokeanalytics.features.home.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
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
     * Represents an intent to manually begin a new day near the configured boundary.
     */
    data object StartNewDay : HomeIntent()

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

    data object OnClickGoals : HomeIntent()

    /**
     * Represents an intent to track a craving (the urge to smoke). The app decides
     * whether to suggest waiting based on the active goal.
     */
    data object TrackCraving : HomeIntent()

    /**
     * Resolves the active craving.
     *
     * @property craving The pending craving being resolved.
     * @property smoked True if the user smoked, false if they let the urge pass.
     */
    data class ResolveCraving(val craving: Craving, val smoked: Boolean) : HomeIntent()

    /**
     * Dismisses the transient "it's already a good time" hint.
     */
    data object DismissCravingHint : HomeIntent()

    /**
     * Dismisses the craving celebration shown after a resolved wait.
     */
    data object DismissCravingCelebration : HomeIntent()

    /**
     * Opens the "what was it related to?" prompt for a given smoke (e.g. from the
     * reminder card, for a smoke that is still untracked).
     */
    data class OpenRelationshipPrompt(val smokeId: String) : HomeIntent()

    /**
     * Saves the tags the user attached to a smoke (built-in keys and/or custom strings).
     */
    data class SaveSmokeRelationship(
        val smokeId: String,
        val tags: Set<String>,
    ) : HomeIntent()

    /**
     * Marks a smoke as having no particular trigger so it stops appearing in the reminder.
     */
    data class SkipSmokeRelationship(val smokeId: String) : HomeIntent()

    /**
     * Closes the relationship prompt without answering; the smoke stays untracked.
     */
    data object DismissRelationshipPrompt : HomeIntent()
}
