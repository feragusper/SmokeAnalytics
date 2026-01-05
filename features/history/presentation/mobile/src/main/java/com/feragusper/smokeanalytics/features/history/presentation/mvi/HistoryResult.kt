package com.feragusper.smokeanalytics.features.history.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlinx.datetime.Instant

/**
 * Represents the results of processing [HistoryIntent], which will modify the view state.
 *
 * This sealed interface defines all possible states the History module can be in,
 * based on the result of processing a user intent.
 */
sealed interface HistoryResult : MVIResult {

    /**
     * Indicates that a loading state is in progress.
     */
    data object Loading : HistoryResult

    /**
     * Indicates that the user is not logged in and the selected date is preserved.
     *
     * @property selectedDate The date that was selected before the user was found to be not logged in.
     */
    data class NotLoggedIn(
        val selectedDate: Instant
    ) : HistoryResult

    /**
     * Indicates that a smoke event was successfully added.
     */
    data object AddSmokeSuccess : HistoryResult

    /**
     * Indicates that a smoke event was successfully edited.
     */
    data object EditSmokeSuccess : HistoryResult

    /**
     * Indicates that a smoke event was successfully deleted.
     */
    data object DeleteSmokeSuccess : HistoryResult

    /**
     * Indicates that navigation to the authentication screen is required.
     */
    data object GoToAuthentication : HistoryResult

    /**
     * Represents errors that might occur during the processing of history intents.
     */
    sealed interface Error : HistoryResult {

        /**
         * A generic error result.
         */
        data object Generic : Error

        /**
         * Error indicating that the user is not logged in.
         */
        data object NotLoggedIn : Error
    }

    /**
     * Indicates a successful fetch of smoke events, containing the fetched data.
     *
     * @property selectedDate The date for which the smoke events were fetched.
     * @property smokes The list of fetched [Smoke] events.
     */
    data class FetchSmokesSuccess(
        val selectedDate: Instant,
        val smokes: List<Smoke>
    ) : HistoryResult

    /**
     * Indicates an error occurred while fetching smoke events.
     */
    data object FetchSmokesError : HistoryResult

    /**
     * Triggers navigation to the previous screen.
     */
    data object NavigateUp : HistoryResult
}