package com.feragusper.smokeanalytics.features.goals.presentation.web.mvi

import com.feragusper.smokeanalytics.features.goals.presentation.web.GoalsViewState
import com.feragusper.smokeanalytics.features.goals.presentation.web.process.GoalsProcessHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsWebStore(
    private val processHolder: GoalsProcessHolder,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private var started = false
    private val intents = Channel<GoalsIntent>(capacity = Channel.Factory.BUFFERED)
    private val _state = MutableStateFlow(GoalsViewState(displayLoading = true))

    val state: StateFlow<GoalsViewState> = _state.asStateFlow()

    fun send(intent: GoalsIntent) {
        intents.trySend(intent)
    }

    fun start() {
        if (started) return
        started = true
        scope.launch {
            intents
                .receiveAsFlow()
                .flatMapLatest { intent -> processHolder.processIntent(intent) }
                .collect { result -> reduce(result) }
        }
        send(GoalsIntent.FetchGoals)
    }

    private fun reduce(result: GoalsResult) {
        val previous = _state.value
        _state.value = when (result) {
            GoalsResult.Loading -> previous.copy(
                displayLoading = true,
                errorMessage = null,
                infoMessage = null,
            )

            is GoalsResult.Loaded -> previous.copy(
                displayLoading = false,
                currentEmail = result.email,
                preferences = result.preferences,
                goalProgress = result.goalProgress,
                errorMessage = null,
                infoMessage = null,
            )

            GoalsResult.LoggedOut -> previous.copy(
                displayLoading = false,
                currentEmail = null,
                goalProgress = null,
                errorMessage = null,
                infoMessage = null,
            )

            GoalsResult.GoalSaved -> previous.copy(
                displayLoading = false,
                errorMessage = null,
                infoMessage = "Saved",
            )

            is GoalsResult.ErrorGeneric -> previous.copy(
                displayLoading = false,
                errorMessage = result.message,
                infoMessage = null,
            )
        }
    }
}
