package com.feragusper.smokeanalytics.features.history.presentation.mvi

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlinx.datetime.Instant

sealed interface HistoryResult {
    data object Loading : HistoryResult

    data class NotLoggedIn(val selectedDate: Instant) : HistoryResult

    data class FetchSmokesSuccess(
        val selectedDate: Instant,
        val smokes: List<Smoke>,
    ) : HistoryResult

    data object FetchSmokesError : HistoryResult

    data object AddSmokeSuccess : HistoryResult
    data object EditSmokeSuccess : HistoryResult
    data object DeleteSmokeSuccess : HistoryResult

    data object NavigateUp : HistoryResult
    data object GoToAuthentication : HistoryResult

    sealed interface Error : HistoryResult {
        data object Generic : Error
        data object NotLoggedIn : Error
    }
}