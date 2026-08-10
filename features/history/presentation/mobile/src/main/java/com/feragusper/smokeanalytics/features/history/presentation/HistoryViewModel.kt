package com.feragusper.smokeanalytics.features.history.presentation

import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult.AddSmokeInFlight
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
import kotlin.time.Clock

class HistoryViewModel constructor(
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
            is Loading -> previous.copy(
                displayLoading = true,
                error = null,
                // Move the header to the new day now, and drop the old list so it skeletonizes.
                // Same day (a post-mutation refresh) keeps the list — per-row skeletons cover it.
                selectedDate = result.selectedDate,
                smokes = if (result.selectedDate != previous.selectedDate) null else previous.smokes,
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

            AddSmokeInFlight -> previous.copy(
                isAddingSmoke = true,
                error = null,
            )

            is NotLoggedIn -> previous.copy(
                displayLoading = false,
                error = Error.NotLoggedIn,
                selectedDate = result.selectedDate,
                smokes = null,
                isAddingSmoke = false,
            )

            is FetchSmokesSuccess -> previous.copy(
                displayLoading = false,
                error = null,
                smokes = result.smokes,
                selectedDate = result.selectedDate,
                monthCounts = result.monthCounts,
                previousMonthCounts = result.previousMonthCounts,
                use24HourClock = result.use24HourClock,
                pendingSmokeId = null,
                pendingAction = null,
                isAddingSmoke = false,
                rowInteractionEpoch = previous.rowInteractionEpoch + 1,
            )

            DeleteSmokeSuccess, EditSmokeSuccess, AddSmokeSuccess -> {
                // Keep the in-flight markers (pendingSmokeId / isAddingSmoke) until the refetch
                // lands. The state is a conflated StateFlow, so if we cleared them here the write's
                // in-flight state would be dropped between this near-instant success and the local
                // write — the skeleton would never render. FetchSmokesSuccess clears them.
                intents().trySend(HistoryIntent.FetchSmokes(previous.selectedDate))
                previous
            }

            is Error -> previous.copy(
                displayLoading = false,
                error = result,
                pendingSmokeId = null,
                pendingAction = null,
                isAddingSmoke = false,
            )

            FetchSmokesError -> previous.copy(
                displayLoading = false,
                error = Error.Generic,
                pendingSmokeId = null,
                pendingAction = null,
                isAddingSmoke = false,
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
