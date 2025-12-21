package com.feragusper.smokeanalytics.features.home.presentation.web

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

class HomeWebStore(
    private val processHolder: HomeProcessHolder,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val intents = Channel<HomeIntent>(capacity = Channel.Factory.BUFFERED)

    private val _state = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state.asStateFlow()

    fun send(intent: HomeIntent) {
        intents.trySend(intent)
    }

    fun start() {
        scope.launch {
            intents
                .receiveAsFlow()
                .flatMapLatest { intent -> processHolder.processIntent(intent) }
                .collect { result -> reduce(result) }
        }

        // bootstrap
        send(HomeIntent.FetchSmokes)
    }

    private fun reduce(result: HomeResult) {
        val previous = _state.value
        val newState = when (result) {
            HomeResult.Loading -> previous.copy(
                displayLoading = true,
                displayRefreshLoading = false,
                error = null,
            )

            HomeResult.RefreshLoading -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = true,
                error = null,
            )

            HomeResult.NotLoggedIn -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = null,
                smokesPerDay = 0,
                smokesPerWeek = 0,
                smokesPerMonth = 0,
                timeSinceLastCigarette = 0L to 0L,
                latestSmokes = emptyList(),
            )

            is HomeResult.FetchSmokesSuccess -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = null,
                smokesPerDay = result.smokeCountListResult.countByToday,
                smokesPerWeek = result.smokeCountListResult.countByWeek,
                smokesPerMonth = result.smokeCountListResult.countByMonth,
                latestSmokes = result.smokeCountListResult.todaysSmokes,
                timeSinceLastCigarette = result.smokeCountListResult.timeSinceLastCigarette,
            )

            is HomeResult.UpdateTimeSinceLastCigarette -> previous.copy(
                timeSinceLastCigarette = result.timeSinceLastCigarette
            )

            HomeResult.AddSmokeSuccess,
            HomeResult.EditSmokeSuccess,
            HomeResult.DeleteSmokeSuccess -> {
                send(HomeIntent.FetchSmokes)
                previous
            }

            is HomeResult.Error -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = result.toHomeError(),
            )

            HomeResult.FetchSmokesError -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = HomeViewState.HomeError.Generic,
            )

            HomeResult.GoToAuthentication,
            HomeResult.GoToHistory -> previous // en web lo resolvemos arriba (router) o por callbacks
        }

        _state.value = newState
    }

    private fun HomeResult.Error.toHomeError(): HomeViewState.HomeError =
        when (this) {
            HomeResult.Error.Generic -> HomeViewState.HomeError.Generic
            HomeResult.Error.NotLoggedIn -> HomeViewState.HomeError.NotLoggedIn
        }
}