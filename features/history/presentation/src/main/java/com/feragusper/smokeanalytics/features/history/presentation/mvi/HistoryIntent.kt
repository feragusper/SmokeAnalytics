package com.feragusper.smokeanalytics.features.history.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import java.time.LocalDateTime

/**
 * Defines intents related to the history feature, representing actions the user can initiate.
 */
sealed class HistoryIntent : MVIIntent {

    /**
     * Requests to edit a specific smoke event.
     */
    data class EditSmoke(val id: String, val date: LocalDateTime) : HistoryIntent()

    /**
     * Requests to delete a specific smoke event.
     */
    data class DeleteSmoke(val id: String) : HistoryIntent()

    /**
     * Requests to add a new smoke event.
     */
    data class AddSmoke(val date: LocalDateTime) : HistoryIntent()

    /**
     * Requests to fetch smoke events for a specific date.
     */
    data class FetchSmokes(val date: LocalDateTime) : HistoryIntent()

    /**
     * Indicates a request to navigate up in the navigation stack.
     */
    object NavigateUp : HistoryIntent()
}
