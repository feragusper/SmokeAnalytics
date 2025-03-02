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
 *
 * @param I The type of [MVIIntent] that this ViewModel processes.
 * @param S The type of [MVIViewState] that this ViewModel maintains.
 * @param R The type of [MVIResult] produced as a result of processing intents.
 * @param N The type of [MVINavigator] used for navigation.
 */
abstract class MVIViewModel<I : MVIIntent, S : MVIViewState<I>, R : MVIResult, N : MVINavigator>(
    initialState: S,
) : ViewModel() {

    /**
     * Navigator instance to handle navigation actions.
     */
    abstract var navigator: N

    /**
     * Channel for receiving user intents.
     */
    private val intentChannel: Channel<I> = Channel(Channel.UNLIMITED)

    /**
     * StateFlow to emit the current state of the UI.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val stateChannel: StateFlow<S> = intentChannel
        .receiveAsFlow()
        .flatMapMerge(transform = ::transformer)
        .scan(initialState, ::reducer)
        .stateIn(viewModelScope, SharingStarted.Lazily, initialState)

    /**
     * Stream events from the view.
     *
     * @return A [Channel] that receives user intentions.
     */
    fun intents(): Channel<I> = intentChannel

    /**
     * Receive states from the view.
     *
     * @return A [StateFlow] emitting the current state of the UI.
     */
    fun states(): StateFlow<S> = stateChannel

    /**
     * Transforms an [MVIIntent] into a flow of [MVIResult]s.
     *
     * @param intent The intent to process.
     * @return A [Flow] emitting [MVIResult]s based on the intent.
     */
    protected abstract fun transformer(intent: I): Flow<R>

    /**
     * Produces a new state from the previous state and a result.
     *
     * @param previous The previous state.
     * @param result The result of an operation that affects the state.
     * @return The new state.
     */
    protected abstract suspend fun reducer(previous: S, result: R): S
}
