package com.feragusper.smokeanalytics.features.history.presentation

import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.AddSmokeSuccess
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.DeleteSmokeSuccess
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.DeleteSmokeInFlight
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.EditSmokeSuccess
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.EditSmokeInFlight
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

    override fun transformer(intent: HistoryIntent) = processHolder.processIntent(intent)

    fun onScreenVisible() {
        intents().trySend(HistoryIntent.FetchSmokes(states().value.selectedDate))
    }

    override fun reducer(
        previous: HistoryViewState,
        result: HistoryResult
    ): HistoryViewState =
        when (result) {
            Loading -> previous.copy(
                displayLoading = true,
                error = null,
            )

            is EditSmokeInFlight -> previous.copy(
                pendingSmokeId = result.id,
                pendingAction = HistoryPendingAction.Editing,
                error = null,
            )

            is DeleteSmokeInFlight -> previous.copy(
                pendingSmokeId = result.id,
                pendingAction = HistoryPendingAction.Deleting,
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
                monthCounts = result.monthCounts,
                pendingSmokeId = null,
                pendingAction = null,
                rowInteractionEpoch = previous.rowInteractionEpoch + 1,
            )

            DeleteSmokeSuccess, EditSmokeSuccess, AddSmokeSuccess -> {
                intents().trySend(HistoryIntent.FetchSmokes(previous.selectedDate))
                previous.copy(
                    pendingSmokeId = null,
                    pendingAction = null,
                    rowInteractionEpoch = previous.rowInteractionEpoch + 1,
                )
            }

            is Error -> previous.copy(
                displayLoading = false,
                error = result,
                pendingSmokeId = null,
                pendingAction = null,
            )

            FetchSmokesError -> previous.copy(
                displayLoading = false,
                error = Error.Generic,
                pendingSmokeId = null,
                pendingAction = null,
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
