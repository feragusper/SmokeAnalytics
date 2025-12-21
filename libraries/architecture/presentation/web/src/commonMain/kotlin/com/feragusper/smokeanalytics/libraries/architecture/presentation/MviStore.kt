package com.feragusper.smokeanalytics.libraries.architecture.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class MviStore<I, S, R>(
    private val scope: CoroutineScope,
    initialState: S,
) {
    private val intents = Channel<I>(Channel.BUFFERED)
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    fun dispatch(intent: I) {
        intents.trySend(intent)
    }

    protected abstract fun transformer(intent: I): Flow<R>
    protected abstract fun reducer(previous: S, result: R): S

    fun start() {
        scope.launch {
            intents
                .receiveAsFlow()
                .flatMapConcat { intent -> transformer(intent) }
                .scan(_state.value) { prev, result -> reducer(prev, result) }
                .collect { _state.value = it }
        }
    }
}