package com.feragusper.smokeanalytics.features.stats.presentation.web.mvi

import com.feragusper.smokeanalytics.features.stats.presentation.web.StatsViewState
import com.feragusper.smokeanalytics.features.stats.presentation.web.process.StatsProcessHolder
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

/**
 * Represents the store for the Stats screen.
 *
 * @property processHolder The process holder for the Stats screen.
 * @property scope The coroutine scope for the store.
 */
class StatsWebStore(
    private val processHolder: StatsProcessHolder,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val intents = Channel<StatsIntent>(capacity = Channel.Factory.BUFFERED)

    private val _state = MutableStateFlow(StatsViewState())

    /**
     * The current state of the Stats screen.
     */
    val state: StateFlow<StatsViewState> = _state.asStateFlow()

    /**
     * Sends an intent to the store.
     *
     * @param intent The intent to send.
     */
    fun send(intent: StatsIntent) {
        intents.trySend(intent)
    }

    /**
     * Starts the store.
     */
    fun start() {
        scope.launch {
            intents
                .receiveAsFlow()
                .flatMapLatest(processHolder::processIntent)
                .collect(::reduce)
        }
    }

    private fun reduce(result: StatsResult) {
        val previous = _state.value
        _state.value = when (result) {
            StatsResult.Loading -> previous.copy(
                displayLoading = true,
                error = null,
            )

            is StatsResult.Success -> previous.copy(
                displayLoading = false,
                stats = result.stats,
                error = null,
            )

            is StatsResult.Error -> previous.copy(
                displayLoading = false,
                stats = null,
                error = StatsViewState.StatsError.Generic,
            )
        }
    }
}