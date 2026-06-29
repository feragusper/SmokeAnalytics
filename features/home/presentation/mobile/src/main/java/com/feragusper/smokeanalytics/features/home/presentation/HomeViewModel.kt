package com.feragusper.smokeanalytics.features.home.presentation

import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.AddSmokeSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.DeleteSmokeSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.EditSmokeSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.Error
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.FetchSmokesSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.GoToAuthentication
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.GoToGoals
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.GoToHistory
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.Loading
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.NotLoggedIn
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.StartNewDaySuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.UpdateTimeSinceLastCigarette
import com.feragusper.smokeanalytics.features.home.domain.elapsedToneFrom
import com.feragusper.smokeanalytics.features.home.presentation.mvi.compose.CravingCelebration
import com.feragusper.smokeanalytics.features.home.presentation.mvi.compose.HomeViewState
import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator
import com.feragusper.smokeanalytics.features.home.presentation.process.HomeProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

/**
 * ViewModel for the Home feature, responsible for processing user intents, interacting with the domain layer,
 * and updating the UI state.
 *
 * It extends [MVIViewModel] to implement the Model-View-Intent (MVI) architecture pattern.
 * This ViewModel handles home screen-related logic and updates the UI state accordingly.
 *
 * @property processHolder Responsible for processing intents and invoking use cases.
 */
class HomeViewModel constructor(
    private val processHolder: HomeProcessHolder,
) : MVIViewModel<HomeIntent, HomeViewState, HomeResult, HomeNavigator>(
    initialState = HomeViewState()
) {

    /**
     * Timer for updating the time since the last cigarette.
     */
    private var timer: Timer? = null

    /**
     * Navigator instance for handling navigation actions.
     */
    override lateinit var navigator: HomeNavigator

    /**
     * Transforms [HomeIntent] into a stream of [HomeResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A Flow of [HomeResult] representing the result of processing the intent.
     */
    override fun transformer(intent: HomeIntent) = processHolder.processIntent(intent)

    fun onScreenVisible() {
        val state = states().value
        val hasCachedData = state.timeSinceLastCigarette != null || state.lastSmoke != null || state.smokesPerDay != null
        intents().trySend(if (hasCachedData) HomeIntent.RefreshFetchSmokes else HomeIntent.FetchSmokes)
    }

    /**
     * Reduces the previous [HomeViewState] and a new [HomeResult] to a new state.
     *
     * This function is responsible for creating the new state based on the current state and the result.
     *
     * @param previous The previous state of the UI.
     * @param result The result of processing the intent.
     * @return The new state of the UI.
     */
    override fun reducer(previous: HomeViewState, result: HomeResult): HomeViewState =
        when (result) {
            Loading -> previous.copy(
                displayLoading = true,
                displayRefreshLoading = false,
                error = null,
            )

            HomeResult.RefreshLoading -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = null,
            )

            NotLoggedIn -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = Error.NotLoggedIn,
                smokesPerDay = null,
                smokesPerWeek = null,
                smokesPerMonth = null,
                timeSinceLastCigarette = null,
                lastSmoke = null,
                goalProgress = null,
                hasActiveGoal = false,
                awakeMinutesPerDay = 0,
                dayStartHour = 0,
                bedtimeHour = 0,
                canStartNewDay = false,
            )

            GoToHistory -> {
                navigator.navigateToHistory()
                previous
            }

            GoToGoals -> {
                navigator.navigateToSettings()
                previous
            }

            GoToAuthentication -> {
                navigator.navigateToAuthentication()
                previous
            }

            is FetchSmokesSuccess -> {
                // Cancel existing timer and create a new one to update the time since the last cigarette
                timer?.cancel()
                timer = fixedRateTimer(
                    name = "timer",
                    period = TimeUnit.MINUTES.toMillis(1),
                ) {
                    intents().trySend(HomeIntent.TickTimeSinceLastCigarette(result.smokeCountListResult.lastSmoke))
                }

                previous.copy(
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
                    rateSummary = result.rateSummary,
                    gamificationSummary = result.gamificationSummary,
                    goalProgress = result.goalProgress,
                    hasActiveGoal = result.preferences.activeGoal != null,
                    awakeMinutesPerDay = result.preferences.awakeMinutesPerDay,
                    dayStartHour = result.preferences.dayStartHour,
                    bedtimeHour = result.preferences.bedtimeHour,
                    canStartNewDay = result.canStartNewDay,
                    locationTrackingAvailability = result.locationTrackingAvailability,
                    elapsedTone = elapsedToneFrom(
                        result.smokeCountListResult.timeSinceLastCigarette.first,
                        result.smokeCountListResult.timeSinceLastCigarette.second,
                    ),
                    monthTrend = if (result.previousMonthCount > 0) {
                        (((result.previousMonthCount - result.smokeCountListResult.countByMonth).toDouble() / result.previousMonthCount) * 100).toInt()
                    } else null,
                    monthTrendDelta = if (result.previousMonthCount > 0) {
                        result.smokeCountListResult.countByMonth - result.previousMonthCount
                    } else null,
                    activeCraving = result.activeCraving,
                    cravingStats = result.cravingStats,
                    pendingRelationshipSmokes = result.pendingRelationshipSmokes,
                )
            }

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
                intents().trySend(HomeIntent.FetchSmokes)
                previous.copy(
                    displayLoading = false,
                    activeCraving = null,
                    cravingCelebration = result.points.takeIf { it > 0 }?.let {
                        CravingCelebration(outcome = result.outcome, points = it)
                    },
                )
            }

            HomeResult.CravingHintDismissed -> previous.copy(showCravingHint = false)

            HomeResult.CravingCelebrationDismissed -> previous.copy(cravingCelebration = null)

            is UpdateTimeSinceLastCigarette -> {
                previous.copy(
                    timeSinceLastCigarette = result.timeSinceLastCigarette,
                    lastSmoke = result.lastSmoke,
                    elapsedTone = elapsedToneFrom(
                        result.timeSinceLastCigarette.first,
                        result.timeSinceLastCigarette.second,
                    ),
                )
            }

            DeleteSmokeSuccess, EditSmokeSuccess, StartNewDaySuccess -> {
                // Re-fetch smokes when editing, deleting, or starting a new day.
                intents().trySend(HomeIntent.FetchSmokes)
                previous.copy(
                    displayLoading = false,
                    displayRefreshLoading = false,
                    error = null,
                )
            }

            is AddSmokeSuccess -> {
                // Re-fetch so the new smoke shows up, and open the relationship prompt for it.
                intents().trySend(HomeIntent.FetchSmokes)
                previous.copy(
                    displayLoading = false,
                    displayRefreshLoading = false,
                    error = null,
                    relationshipPromptSmokeId = result.smokeId,
                )
            }

            HomeResult.RelationshipUpdated -> {
                // Relationship saved/skipped: refresh the pending list and close the prompt.
                intents().trySend(HomeIntent.FetchSmokes)
                previous.copy(relationshipPromptSmokeId = null)
            }

            HomeResult.RelationshipPromptDismissed -> previous.copy(relationshipPromptSmokeId = null)

            is Error -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = result,
            )
        }
}
