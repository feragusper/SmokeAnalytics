package com.feragusper.smokeanalytics.features.settings.presentation.web

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

class SettingsWebStore(
    private val processHolder: SettingsProcessHolder,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val intents = Channel<SettingsIntent>(capacity = Channel.Factory.BUFFERED)

    private val _state = MutableStateFlow(SettingsViewState())
    val state: StateFlow<SettingsViewState> = _state.asStateFlow()

    fun send(intent: SettingsIntent) {
        intents.trySend(intent)
    }

    fun start() {
        scope.launch {
            intents
                .receiveAsFlow()
                .flatMapLatest { intent -> processHolder.processIntent(intent) }
                .collect { result -> reduce(result) }
        }

        send(SettingsIntent.FetchUser)
    }

    private fun reduce(result: SettingsResult) {
        val previous = _state.value
        val newState = when (result) {
            SettingsResult.Loading -> previous.copy(
                displayLoading = true,
                errorMessage = null,
            )

            is SettingsResult.UserLoggedIn -> previous.copy(
                displayLoading = false,
                currentEmail = result.email,
                errorMessage = null,
            )

            SettingsResult.UserLoggedOut -> previous.copy(
                displayLoading = false,
                currentEmail = null,
                errorMessage = null,
            )

            SettingsResult.ErrorGeneric -> previous.copy(
                displayLoading = false,
                errorMessage = "Something went wrong",
            )
        }

        _state.value = newState
    }
}