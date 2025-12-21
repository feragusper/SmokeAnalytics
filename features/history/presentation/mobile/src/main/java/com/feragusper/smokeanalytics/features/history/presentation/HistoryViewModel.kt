package com.feragusper.smokeanalytics.features.history.presentation

import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.AddSmokeSuccess
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.DeleteSmokeSuccess
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.EditSmokeSuccess
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.Error
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.FetchSmokesError
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.FetchSmokesSuccess
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.GoToAuthentication
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.Loading
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.NavigateUp
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.NotLoggedIn
import com.feragusper.smokeanalytics.features.history.presentation.mvi.compose.HistoryViewState
import com.feragusper.smokeanalytics.features.history.presentation.navigation.HistoryNavigator
import com.feragusper.smokeanalytics.features.history.presentation.process.HistoryProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val processHolder: HistoryProcessHolder,
) : MVIViewModel<HistoryIntent, HistoryViewState, HistoryResult, HistoryNavigator>(
    initialState = HistoryViewState()
) {

    override lateinit var navigator: HistoryNavigator

    init {
        intents().trySend(HistoryIntent.FetchSmokes(Clock.System.now()))
    }

    override fun transformer(intent: HistoryIntent) = processHolder.processIntent(intent)

    override fun reducer(
        previous: HistoryViewState,
        result: HistoryResult
    ): HistoryViewState =
        when (result) {
            Loading -> previous.copy(
                displayLoading = true,
                error = null,
            )

            is NotLoggedIn -> previous.copy(
                displayLoading = false,
                error = null,
                selectedDate = result.selectedDate,
            )

            is FetchSmokesSuccess -> previous.copy(
                displayLoading = false,
                error = null,
                smokes = result.smokes,
                selectedDate = result.selectedDate,
            )

            DeleteSmokeSuccess, EditSmokeSuccess, AddSmokeSuccess -> {
                intents().trySend(HistoryIntent.FetchSmokes(previous.selectedDate))
                previous
            }

            is Error -> previous.copy(
                displayLoading = false,
                error = result,
            )

            FetchSmokesError -> previous.copy(
                displayLoading = false,
                error = Error.Generic,
            )

            NavigateUp -> {
                navigator.navigateUp()
                previous
            }

            GoToAuthentication -> {
                navigator.navigateToAuthentication()
                previous
            }
        }
}