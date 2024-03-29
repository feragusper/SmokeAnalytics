package com.feragusper.smokeanalytics.libraries.architecture.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

/**
 * A ViewModel designed to handle MVI-based state management, providing a structured way to manage state
 * changes and user intentions in a reactive manner.
 */
abstract class MVIViewModel<I : MVIIntent, S : MVIViewState<I>, R : MVIResult, N : MVINavigator>(
    initialState: S,
) : ViewModel() {

    abstract var navigator: N

    private val intentChannel: Channel<I> = Channel(Channel.UNLIMITED)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stateChannel: StateFlow<S> = intentChannel
        .receiveAsFlow()
        .flatMapMerge(transform = ::transformer)
        .scan(initialState, ::reducer)
        .stateIn(viewModelScope, SharingStarted.Lazily, initialState)

    /**
     * Stream events from the view
     */
    fun intents(): Channel<I> = intentChannel

    /**
     * Receive states from the view
     */
    fun states(): StateFlow<S> = stateChannel

    /**
     * Processor to transform an intent into a flow of results
     */
    protected abstract suspend fun transformer(intent: I): Flow<R>

    /**
     * Receives an state with an operation result to generate a new state
     */
    protected abstract suspend fun reducer(previous: S, result: R): S
}