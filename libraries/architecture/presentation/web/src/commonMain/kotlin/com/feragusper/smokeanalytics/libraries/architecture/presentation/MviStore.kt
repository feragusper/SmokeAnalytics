package com.feragusper.smokeanalytics.libraries.architecture.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

/**
 * Represents a MVI store.
 *
 * @param I The type of intents.
 * @param S The type of states.
 * @param R The type of results.
 * @property scope The coroutine scope.
 * @property initialState The initial state.
 */
abstract class MviStore<I, S, R>(
    private val scope: CoroutineScope,
    initialState: S,
) {
    private val intents = Channel<I>(Channel.BUFFERED)
    private val _state = MutableStateFlow(initialState)

    /**
     * The current state.
     */
    val state: StateFlow<S> = _state.asStateFlow()

    /**
     * Dispatches an intent.
     *
     * @param intent The intent to dispatch.
     */
    fun dispatch(intent: I) {
        intents.trySend(intent)
    }

    protected abstract fun transformer(intent: I): Flow<R>
    protected abstract fun reducer(previous: S, result: R): S

    /**
     * Starts the store.
     *
     * @return The store.
     */
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