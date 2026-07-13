package com.feragusper.smokeanalytics.features.home.presentation.web.mvi

import com.feragusper.smokeanalytics.features.home.presentation.web.HomeViewState
import com.feragusper.smokeanalytics.features.home.presentation.web.process.HomeProcessHolder
import com.feragusper.smokeanalytics.features.home.domain.elapsedToneFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
    private var started = false

    private val _state = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state.asStateFlow()

    fun send(intent: HomeIntent) {
        intents.trySend(intent)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        if (started) return
        started = true
        scope.launch {
            intents
                .receiveAsFlow()
                .flatMapLatest { intent -> processHolder.processIntent(intent) }
                .collect { result -> reduce(result) }
        }

        scope.launch {
            while (true) {
                delay(60_000)
                val lastSmoke = _state.value.lastSmoke ?: continue
                send(HomeIntent.TickTimeSinceLastCigarette(lastSmoke))
            }
        }
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
                error = HomeViewState.HomeError.NotLoggedIn,
                smokesPerDay = null,
                smokesPerWeek = null,
                smokesPerMonth = null,
                timeSinceLastCigarette = null,
                latestSmokes = emptyList(),
                lastSmoke = null,
                goalProgress = null,
                hasActiveGoal = false,
                awakeMinutesPerDay = 0,
                dayStartHour = 0,
                bedtimeHour = 0,
                canStartNewDay = false,
            )

            is HomeResult.FetchSmokesSuccess -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = null,
                smokesPerDay = result.smokeCountListResult.countByToday,
                smokesPerWeek = result.smokeCountListResult.countByWeek,
                smokesPerMonth = result.smokeCountListResult.countByMonth,
                latestSmokes = result.smokeCountListResult.todaysSmokes,
                lastSmoke = result.smokeCountListResult.lastSmoke,
                timeSinceLastCigarette = result.smokeCountListResult.timeSinceLastCigarette,
                greetingTitle = result.greetingState.title,
                greetingMessage = result.greetingState.message,
                quitReason = result.preferences.quitReason,
                use24HourClock = result.preferences.use24HourClock,
                cigarettePrice = result.preferences.cigarettePrice,
                homeHeroChoice = result.preferences.homeHeroChoice,
                financialSummary = result.financialSummary,
                rateSummary = result.rateSummary,
                gamificationSummary = result.gamificationSummary,
                goalProgress = result.goalProgress,
                hasActiveGoal = result.preferences.activeGoal != null,
                awakeMinutesPerDay = result.preferences.awakeMinutesPerDay,
                dayStartHour = result.preferences.dayStartHour,
                bedtimeHour = result.preferences.bedtimeHour,
                currencySymbol = result.preferences.currencySymbol,
                canStartNewDay = result.canStartNewDay,
                monthTrend = if (result.previousMonthCount > 0) {
                    (((result.previousMonthCount - result.smokeCountListResult.countByMonth).toDouble() / result.previousMonthCount) * 100).toInt()
                } else {
                    null
                },
                monthTrendDelta = if (result.previousMonthCount > 0) {
                    result.smokeCountListResult.countByMonth - result.previousMonthCount
                } else {
                    null
                },
                locationTrackingAvailability = result.locationTrackingAvailability,
                elapsedTone = elapsedToneFrom(
                    result.smokeCountListResult.timeSinceLastCigarette.first,
                    result.smokeCountListResult.timeSinceLastCigarette.second,
                ),
                activeCraving = result.activeCraving,
                cravingStats = result.cravingStats,
                pendingRelationshipSmokes = result.pendingRelationshipSmokes,
                availableTriggers = result.availableTriggers,
            )

            is HomeResult.CravingTracked -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = null,
                activeCraving = result.craving,
                showCravingHint = false,
            )

            HomeResult.CravingNoWaitNeeded -> previous.copy(
                displayLoading = false,
                showCravingHint = true,
            )

            is HomeResult.CravingResolved -> {
                send(HomeIntent.FetchSmokes)
                previous.copy(
                    displayLoading = false,
                    activeCraving = null,
                    cravingCelebration = result.points.takeIf { it > 0 }?.let {
                        HomeViewState.CravingCelebration(outcome = result.outcome, points = it)
                    },
                )
            }

            HomeResult.CravingHintDismissed -> previous.copy(showCravingHint = false)

            HomeResult.CravingCelebrationDismissed -> previous.copy(cravingCelebration = null)

            is HomeResult.UpdateTimeSinceLastCigarette -> previous.copy(
                timeSinceLastCigarette = result.timeSinceLastCigarette,
                lastSmoke = result.lastSmoke,
                elapsedTone = elapsedToneFrom(
                    result.timeSinceLastCigarette.first,
                    result.timeSinceLastCigarette.second,
                ),
            )

            HomeResult.StartNewDaySuccess,
            HomeResult.EditSmokeSuccess,
            HomeResult.DeleteSmokeSuccess -> {
                send(HomeIntent.FetchSmokes)
                previous
            }

            is HomeResult.AddSmokeSuccess -> {
                // Silent refresh: a full FetchSmokes swapped the page to its loading skeleton
                // behind the relationship prompt.
                send(HomeIntent.RefreshFetchSmokes)
                previous.copy(relationshipPromptSmokeId = result.smokeId)
            }

            HomeResult.RelationshipUpdated -> {
                send(HomeIntent.RefreshFetchSmokes)
                previous.copy(relationshipPromptSmokeId = null)
            }

            HomeResult.RelationshipPromptDismissed -> previous.copy(relationshipPromptSmokeId = null)

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
            HomeResult.GoToHistory,
            HomeResult.GoToGoals -> previous // en web lo resolvemos arriba (router) o por callbacks
        }

        _state.value = newState
    }

    private fun HomeResult.Error.toHomeError(): HomeViewState.HomeError =
        when (this) {
            HomeResult.Error.Generic -> HomeViewState.HomeError.Generic
            HomeResult.Error.NotLoggedIn -> HomeViewState.HomeError.NotLoggedIn
        }
}
