package com.feragusper.smokeanalytics.features.history.presentation.mvi

import com.feragusper.smokeanalytics.features.history.presentation.HistoryViewState
import com.feragusper.smokeanalytics.features.history.presentation.process.HistoryProcessHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Represents the store for the History screen.
 *
 * @property processHolder The process holder for the History screen.
 * @property scope The coroutine scope for the store.
 */
class HistoryWebStore(
    private val processHolder: HistoryProcessHolder,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val intents = Channel<HistoryIntent>(capacity = Channel.Factory.BUFFERED)

    private val _state = MutableStateFlow(HistoryViewState())

    /**
     * The current state of the History screen.
     */
    val state: StateFlow<HistoryViewState> = _state.asStateFlow()

    /**
     * Sends an intent to the store.
     *
     * @param intent The intent to send.
     */
    fun send(intent: HistoryIntent) {
        intents.trySend(intent)
    }

    /**
     * Starts the store.
     */
    fun start() {
        scope.launch {
            intents
                .receiveAsFlow()
                .flatMapLatest { processHolder.processIntent(it) }
                .collect { reduce(it) }
        }

        send(HistoryIntent.FetchSmokes(Clock.System.now()))
    }

    private fun reduce(result: HistoryResult) {
        val prev = _state.value
        val next = when (result) {
            HistoryResult.Loading -> prev.copy(displayLoading = true, error = null)

            is HistoryResult.NotLoggedIn -> prev.copy(
                displayLoading = false,
                error = null,
                selectedDate = result.selectedDate,
                smokes = emptyList(),
            )

            is HistoryResult.FetchSmokesSuccess -> prev.copy(
                displayLoading = false,
                error = null,
                selectedDate = result.selectedDate,
                smokes = result.smokes,
            )

            HistoryResult.FetchSmokesError -> prev.copy(
                displayLoading = false,
                error = HistoryResult.Error.Generic,
            )

            HistoryResult.AddSmokeSuccess,
            HistoryResult.EditSmokeSuccess,
            HistoryResult.DeleteSmokeSuccess -> {
                send(HistoryIntent.FetchSmokes(prev.selectedDate))
                prev
            }

            is HistoryResult.Error -> prev.copy(displayLoading = false, error = result)

            HistoryResult.NavigateUp,
            HistoryResult.GoToAuthentication -> prev
        }

        _state.value = next
    }
}