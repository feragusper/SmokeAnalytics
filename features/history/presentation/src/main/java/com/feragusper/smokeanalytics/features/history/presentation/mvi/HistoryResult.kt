package com.feragusper.smokeanalytics.features.history.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import java.time.LocalDateTime

sealed interface HistoryResult : MVIResult {
    object Loading : HistoryResult
    object NotLoggedIn : HistoryResult
    object AddSmokeSuccess : HistoryResult
    object EditSmokeSuccess : HistoryResult
    object DeleteSmokeSuccess : HistoryResult
    sealed interface Error : HistoryResult {
        object Generic :
            Error

        object NotLoggedIn :
            Error
    }

    data class FetchSmokesSuccess(
        val selectedDate: LocalDateTime,
        val smokes: List<Smoke>
    ) : HistoryResult

    object FetchSmokesError : HistoryResult
    object NavigateUp : HistoryResult
}
