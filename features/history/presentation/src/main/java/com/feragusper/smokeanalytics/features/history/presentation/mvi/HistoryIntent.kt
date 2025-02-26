package com.feragusper.smokeanalytics.features.history.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import java.time.LocalDateTime

/**
 * Defines intents related to the history feature, representing actions the user can initiate.
 *
 * This sealed class represents all possible user actions within the History module,
 * allowing the ViewModel to handle them in a structured manner.
 */
sealed class HistoryIntent : MVIIntent {

    /**
     * Requests to edit a specific smoke event.
     *
     * @property id The unique identifier of the smoke event to be edited.
     * @property date The new date and time for the smoke event.
     */
    data class EditSmoke(val id: String, val date: LocalDateTime) : HistoryIntent()

    /**
     * Requests to delete a specific smoke event.
     *
     * @property id The unique identifier of the smoke event to be deleted.
     */
    data class DeleteSmoke(val id: String) : HistoryIntent()

    /**
     * Requests to add a new smoke event.
     *
     * @property date The date and time when the new smoke event occurred.
     */
    data class AddSmoke(val date: LocalDateTime) : HistoryIntent()

    /**
     * Requests to fetch smoke events for a specific date.
     *
     * @property date The date for which smoke events should be fetched.
     */
    data class FetchSmokes(val date: LocalDateTime) : HistoryIntent()

    /**
     * Indicates a request to navigate up in the navigation stack.
     */
    object NavigateUp : HistoryIntent()
}
