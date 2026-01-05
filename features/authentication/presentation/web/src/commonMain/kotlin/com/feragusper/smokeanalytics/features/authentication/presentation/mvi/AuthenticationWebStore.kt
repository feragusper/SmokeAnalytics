package com.feragusper.smokeanalytics.features.authentication.presentation.mvi

import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationViewState
import com.feragusper.smokeanalytics.features.authentication.presentation.process.AuthenticationProcessHolder
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
 * Manages the state for the authentication feature, acting as a central hub for UI-related data.
 * This class follows a Model-View-Intent (MVI) pattern, where it consumes [AuthenticationIntent]s,
 * processes them via the [AuthenticationProcessHolder], and emits [com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationViewState] updates.
 *
 * It is responsible for:
 * 1. Receiving intents from the UI (e.g., login attempts, user session checks).
 * 2. Delegating the business logic for these intents to the [processHolder].
 * 3. Reducing the results from the process holder into a new view state.
 * 4. Exposing the current [com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationViewState] as a [StateFlow] for the UI to observe.
 *
 * @property processHolder The processor that contains the business logic for handling authentication intents.
 * @property scope The [CoroutineScope] in which the store's logic runs. Defaults to a scope with a [SupervisorJob] and [Dispatchers.Default].
 */
class AuthenticationWebStore(
    private val processHolder: AuthenticationProcessHolder,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val intents = Channel<AuthenticationIntent>(capacity = Channel.Factory.BUFFERED)

    private val _state = MutableStateFlow(AuthenticationViewState())
    val state: StateFlow<AuthenticationViewState> = _state.asStateFlow()

    /**
     * Sends an [AuthenticationIntent] to the store for processing.
     *
     * @param intent The intent to be processed.
     */
    fun send(intent: AuthenticationIntent) {
        intents.trySend(intent)
    }

    /**
     * Initializes the store, starting the flow of processing intents and updating the state.
     *
     * This function launches a coroutine that listens for incoming [AuthenticationIntent]s from a channel.
     * Each intent is processed by the [AuthenticationProcessHolder], and the resulting [AuthenticationResult]
     * is used to update the view state.
     *
     * It also sends an initial `FetchUser` intent to check the current user's session status upon startup.
     */
    fun start() {
        scope.launch {
            intents
                .receiveAsFlow()
                .flatMapLatest { intent -> processHolder.processIntent(intent) }
                .collect { result -> reduce(result) }
        }

        send(AuthenticationIntent.FetchUser)
    }

    private fun reduce(result: AuthenticationResult) {
        val previous = _state.value

        val newState = when (result) {
            AuthenticationResult.Loading -> previous.copy(
                displayLoading = true,
                error = null,
            )

            AuthenticationResult.UserLoggedIn -> previous.copy(
                displayLoading = false,
                isLoggedIn = true,
                error = null,
            )

            AuthenticationResult.UserLoggedOut -> previous.copy(
                displayLoading = false,
                isLoggedIn = false,
                error = null,
            )

            is AuthenticationResult.Error -> previous.copy(
                displayLoading = false,
                isLoggedIn = false,
                error = result.toAuthError(),
            )

            AuthenticationResult.NavigateUp -> previous
        }

        _state.value = newState
    }

    private fun AuthenticationResult.Error.toAuthError(): AuthenticationViewState.AuthenticationError =
        when (this) {
            AuthenticationResult.Error.Generic -> AuthenticationViewState.AuthenticationError.Generic
        }
}