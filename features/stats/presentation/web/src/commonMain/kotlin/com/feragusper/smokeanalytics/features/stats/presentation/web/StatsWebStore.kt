package com.feragusper.smokeanalytics.features.stats.presentation.web

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

class StatsWebStore(
    private val processHolder: StatsProcessHolder,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val intents = Channel<StatsIntent>(capacity = Channel.Factory.BUFFERED)

    private val _state = MutableStateFlow(StatsViewState())
    val state: StateFlow<StatsViewState> = _state.asStateFlow()

    fun send(intent: StatsIntent) {
        intents.trySend(intent)
    }

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