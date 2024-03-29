package com.feragusper.smokeanalytics.features.history.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import java.time.LocalDateTime

/**
 * Represents the results of processing [HistoryIntent], which will modify the view state.
 */
sealed interface HistoryResult : MVIResult {
    object Loading : HistoryResult
    object NotLoggedIn : HistoryResult
    object AddSmokeSuccess : HistoryResult
    object EditSmokeSuccess : HistoryResult
    object DeleteSmokeSuccess : HistoryResult

    /**
     * Represents errors that might occur during the processing of history intents.
     */
    sealed interface Error : HistoryResult {
        object Generic :
            Error

        object NotLoggedIn :
            Error
    }

    /**
     * Indicates a successful fetch of smoke events, containing the fetched data.
     */
    data class FetchSmokesSuccess(
        val selectedDate: LocalDateTime,
        val smokes: List<Smoke>
    ) : HistoryResult

    object FetchSmokesError : HistoryResult
    object NavigateUp : HistoryResult
}
