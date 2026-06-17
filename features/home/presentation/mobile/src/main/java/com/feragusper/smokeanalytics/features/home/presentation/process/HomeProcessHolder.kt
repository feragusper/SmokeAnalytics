package com.feragusper.smokeanalytics.features.home.presentation.process

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.domain.financialSummary
import com.feragusper.smokeanalytics.features.home.domain.gamificationSummary
import com.feragusper.smokeanalytics.features.home.domain.greetingStateFor
import com.feragusper.smokeanalytics.features.home.domain.rateSummary
import com.feragusper.smokeanalytics.features.home.domain.toWidgetSnapshot
import com.feragusper.smokeanalytics.features.goals.domain.EvaluateGoalProgressUseCase
import com.feragusper.smokeanalytics.features.goals.domain.goalDataFetchStart
import com.feragusper.smokeanalytics.libraries.cravings.domain.CravingWaitCalculator
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.toCravingStats
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.AddCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.FetchActiveCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.FetchCravingsUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.ResolveCravingUseCase
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.shouldOfferStartNewDay
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetRefreshService
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.catchAndLog
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.debugSummary
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.SyncWithWearUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import kotlin.time.Clock

/**
 * Processes intents from the Home feature, invoking the appropriate use cases and updating the result.
 *
 * This class is responsible for transforming [HomeIntent] into [HomeResult]s,
 * encapsulating the application's business logic for managing smoke events on the Home screen.
 *
 * @property addSmokeUseCase Use case for adding a new smoke entry.
 * @property editSmokeUseCase Use case for editing an existing smoke entry.
 * @property deleteSmokeUseCase Use case for deleting an existing smoke entry.
 * @property fetchSmokeCountListUseCase Use case for fetching smoke counts and latest smokes.
 * @property fetchSessionUseCase Use case for fetching the current session state.
 */
class HomeProcessHolder constructor(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val editSmokeUseCase: EditSmokeUseCase,
    private val deleteSmokeUseCase: DeleteSmokeUseCase,
    private val fetchSmokeCountListUseCase: FetchSmokeCountListUseCase,
    private val fetchSmokesUseCase: FetchSmokesUseCase,
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val syncWithWearUseCase: SyncWithWearUseCase,
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    private val locationCaptureService: LocationCaptureService,
    private val widgetRefreshService: WidgetRefreshService,
    private val addCravingUseCase: AddCravingUseCase,
    private val fetchActiveCravingUseCase: FetchActiveCravingUseCase,
    private val fetchCravingsUseCase: FetchCravingsUseCase,
    private val resolveCravingUseCase: ResolveCravingUseCase,
    private val evaluateGoalProgressUseCase: EvaluateGoalProgressUseCase = EvaluateGoalProgressUseCase(),
    private val cravingWaitCalculator: CravingWaitCalculator = CravingWaitCalculator(),
) : MVIProcessHolder<HomeIntent, HomeResult> {

    /**
     * Processes an [HomeIntent] and transforms it into a stream of [HomeResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A [Flow] emitting the corresponding [HomeResult]s.
     */
    override fun processIntent(intent: HomeIntent): Flow<HomeResult> = when (intent) {
        HomeIntent.FetchSmokes, HomeIntent.RefreshFetchSmokes -> processFetchSmokes(intent is HomeIntent.RefreshFetchSmokes)
        HomeIntent.AddSmoke -> processAddSmoke()
        HomeIntent.StartNewDay -> processStartNewDay()
        HomeIntent.OnClickHistory -> flow { emit(HomeResult.GoToHistory) }
        HomeIntent.OnClickGoals -> flow { emit(HomeResult.GoToGoals) }
        is HomeIntent.TickTimeSinceLastCigarette -> processTickTimeSinceLastCigarette(intent)
        is HomeIntent.EditSmoke -> processEditSmoke(intent)
        is HomeIntent.DeleteSmoke -> processDeleteSmoke(intent)
        HomeIntent.TrackCraving -> processTrackCraving()
        is HomeIntent.ResolveCraving -> processResolveCraving(intent)
        HomeIntent.DismissCravingHint -> flow { emit(HomeResult.CravingHintDismissed) }
        HomeIntent.DismissCravingCelebration -> flow { emit(HomeResult.CravingCelebrationDismissed) }
    }

    /**
     * Handles the [HomeIntent.FetchSmokes] and [HomeIntent.RefreshFetchSmokes] intents by fetching the list of smoke events.
     *
     * It checks the user's session state before fetching and differentiates between a regular and refresh loading state.
     *
     * @param isRefresh Indicates if this is a refresh request.
     * @return A [Flow] emitting the result of the fetch operation.
     */
    private fun processFetchSmokes(isRefresh: Boolean) = flow {
        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> emit(HomeResult.NotLoggedIn)
            is Session.LoggedIn -> {
                emit(if (isRefresh) HomeResult.RefreshLoading else HomeResult.Loading)
                val preferences = fetchUserPreferencesUseCase()
                val locationTrackingAvailability = locationCaptureService.locationTrackingAvailability(
                    preferences.locationTrackingEnabled
                )
                val smokeCounts = fetchSmokeCountListUseCase.invoke(
                    dayStartHour = preferences.dayStartHour,
                    manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                )
                val timeZone = TimeZone.currentSystemDefault()
                val now = Clock.System.now()
                val today = now.toLocalDateTime(timeZone).date
                val currentMonthStart = LocalDate(today.year, today.monthNumber, 1).atStartOfDayIn(timeZone)
                val previousMonthStart = LocalDate(today.year, today.monthNumber, 1)
                    .minus(DatePeriod(months = 1))
                    .atStartOfDayIn(timeZone)
                // Compare equal windows: current month elapsed so far vs the same elapsed span
                // of the previous month. Otherwise a partial current month is always measured
                // against a full previous month and the trend is permanently skewed downward.
                // Clamp the previous window to the previous month so a longer current month
                // (e.g. comparing a 31-day month against February) can't spill into the current one.
                val monthElapsed = now - currentMonthStart
                val previousMonthWindowEnd =
                    minOf(previousMonthStart + monthElapsed, currentMonthStart)
                val previousMonthCount =
                    fetchSmokesUseCase(start = previousMonthStart, end = previousMonthWindowEnd).size
                val goalSmokes = fetchSmokesUseCase(start = goalDataFetchStart(preferences))
                val goalProgress = evaluateGoalProgressUseCase(preferences.activeGoal, goalSmokes, preferences)
                val activeCraving = fetchActiveCravingUseCase()
                val cravingStats = fetchCravingsUseCase(start = goalDataFetchStart(preferences)).toCravingStats()
                val greetingState = greetingStateFor(
                    hourOfDay = Clock.System.now()
                        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).hour,
                    todayCount = smokeCounts.countByToday,
                    currentStreakHours = smokeCounts.timeSinceLastCigarette.first,
                )
                emit(
                    HomeResult.FetchSmokesSuccess(
                        smokeCountListResult = smokeCounts,
                        preferences = preferences,
                        greetingState = greetingState.copy(
                            title = personalizeGreetingTitle(
                                greetingTitle = greetingState.title,
                                displayName = session.user.displayName,
                            )
                        ),
                        financialSummary = financialSummary(
                            todayCount = smokeCounts.countByToday,
                            weekCount = smokeCounts.countByWeek,
                            monthCount = smokeCounts.countByMonth,
                            preferences = preferences,
                        ),
                        rateSummary = rateSummary(
                            smokeCountListResult = smokeCounts,
                            preferences = preferences,
                        ),
                        gamificationSummary = gamificationSummary(smokeCounts.todaysSmokes),
                        goalProgress = goalProgress,
                        canStartNewDay = shouldOfferStartNewDay(
                            dayStartHour = preferences.dayStartHour,
                            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                        ),
                        locationTrackingAvailability = locationTrackingAvailability,
                        previousMonthCount = previousMonthCount,
                        activeCraving = activeCraving,
                        cravingStats = cravingStats,
                    )
                )
                widgetRefreshService.refreshHomeSnapshot(smokeCounts.toWidgetSnapshot(preferences, goalProgress))
            }
        }
    }.catchAndLog { e ->
        emit(HomeResult.Error.Generic(e.debugSummary()))
    }

    /**
     * Handles the [HomeIntent.DeleteSmoke] intent by deleting the specified smoke event.
     *
     * @param intent The delete smoke intent containing the smoke ID to be deleted.
     * @return A [Flow] emitting the result of the delete operation.
     */
    private fun processDeleteSmoke(intent: HomeIntent.DeleteSmoke) = flow {
        emit(HomeResult.Loading)
        deleteSmokeUseCase.invoke(intent.id)
        emit(HomeResult.DeleteSmokeSuccess)
        refreshWidgetSnapshotBestEffort()
        syncWithWearBestEffort()
    }.catchAndLog { e ->
        emit(HomeResult.Error.Generic(e.debugSummary()))
    }

    /**
     * Handles the [HomeIntent.EditSmoke] intent by editing the specified smoke event.
     *
     * @param intent The edit smoke intent containing the smoke ID and new date.
     * @return A [Flow] emitting the result of the edit operation.
     */
    private fun processEditSmoke(intent: HomeIntent.EditSmoke) = flow {
        emit(HomeResult.Loading)
        editSmokeUseCase.invoke(intent.id, intent.date)
        emit(HomeResult.EditSmokeSuccess)
        refreshWidgetSnapshotBestEffort()
        syncWithWearBestEffort()
    }.catchAndLog { e ->
        emit(HomeResult.Error.Generic(e.debugSummary()))
    }

    /**
     * Handles the [HomeIntent.TickTimeSinceLastCigarette] intent by calculating the time since the last cigarette.
     *
     * @param intent The tick intent containing the last cigarette event.
     * @return A [Flow] emitting the updated time since the last cigarette.
     */
    private fun processTickTimeSinceLastCigarette(intent: HomeIntent.TickTimeSinceLastCigarette) =
        flow {
            emit(
                HomeResult.UpdateTimeSinceLastCigarette(
                    timeSinceLastCigarette = intent.lastCigarette?.date?.timeElapsedSinceNow() ?: (0L to 0L),
                    lastSmoke = intent.lastCigarette,
                )
            )
        }

    /**
     * Handles the [HomeIntent.AddSmoke] intent by adding a new smoke event.
     * It checks the user's session state before adding the smoke.
     *
     * @return A [Flow] emitting the result of the add operation.
     */
    private fun processAddSmoke(): Flow<HomeResult> = flow {
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> {
                emit(HomeResult.Error.NotLoggedIn)
                emit(HomeResult.GoToAuthentication)
            }

            is Session.LoggedIn -> {
                emit(HomeResult.Loading)
                val preferences = fetchUserPreferencesUseCase()
                val locationAvailability = locationCaptureService.locationTrackingAvailability(
                    preferences.locationTrackingEnabled
                )
                val location = if (locationAvailability.isReady) {
                    locationCaptureService.captureCurrentLocation()?.let {
                        GeoPoint(latitude = it.latitude, longitude = it.longitude)
                    }
                } else {
                    null
                }
                addSmokeUseCase.invoke(location = location)
                emit(HomeResult.AddSmokeSuccess)
                refreshWidgetSnapshotBestEffort()
                syncWithWearBestEffort()
            }
        }
    }.catchAndLog { e ->
        Timber.e(e, "Track smoke failed")
        emit(HomeResult.Error.Generic(e.debugSummary()))
    }

    /**
     * Handles [HomeIntent.TrackCraving]. Works out whether the active goal says it
     * is time to smoke. If not, records a pending craving with the time the user is
     * allowed to smoke again so the UI can show a countdown.
     */
    private fun processTrackCraving(): Flow<HomeResult> = flow {
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> {
                emit(HomeResult.Error.NotLoggedIn)
                emit(HomeResult.GoToAuthentication)
            }

            is Session.LoggedIn -> {
                val preferences = fetchUserPreferencesUseCase()
                val smokeCounts = fetchSmokeCountListUseCase(
                    dayStartHour = preferences.dayStartHour,
                    manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                )
                val advice = cravingWaitCalculator(
                    activeGoal = preferences.activeGoal,
                    lastSmokeAt = smokeCounts.lastSmoke?.date,
                    preferences = preferences,
                )
                if (advice.canSmokeNow) {
                    emit(HomeResult.CravingNoWaitNeeded)
                } else {
                    val craving = addCravingUseCase(targetAt = advice.nextAllowedAt)
                    emit(HomeResult.CravingTracked(craving))
                }
            }
        }
    }.catchAndLog { e ->
        Timber.e(e, "Track craving failed")
        emit(HomeResult.Error.Generic(e.debugSummary()))
    }

    /**
     * Handles [HomeIntent.ResolveCraving]. If the user let the urge pass it is a
     * resist; if they smoked it is a postponed cigarette (when the wait completed)
     * or giving in (when it did not), and the cigarette is logged.
     */
    private fun processResolveCraving(intent: HomeIntent.ResolveCraving): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        val outcome = if (!intent.smoked) {
            CravingOutcome.RESISTED
        } else {
            val target = intent.craving.targetAt
            if (target != null && Clock.System.now() >= target) {
                CravingOutcome.POSTPONED
            } else {
                CravingOutcome.GAVE_IN
            }
        }
        val points = resolveCravingUseCase(intent.craving, outcome)
        if (intent.smoked) {
            addSmokeUseCase()
        }
        emit(HomeResult.CravingResolved(outcome = outcome, points = points))
        refreshWidgetSnapshotBestEffort()
        syncWithWearBestEffort()
    }.catchAndLog { e ->
        Timber.e(e, "Resolve craving failed")
        emit(HomeResult.Error.Generic(e.debugSummary()))
    }

    private fun processStartNewDay(): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        val preferences = fetchUserPreferencesUseCase()
        updateUserPreferencesUseCase(
            preferences.copy(manualDayStartEpochMillis = Clock.System.now().toEpochMilliseconds())
        )
        emit(HomeResult.StartNewDaySuccess)
    }.catchAndLog { e ->
        emit(HomeResult.Error.Generic(e.debugSummary()))
    }

    private suspend fun refreshWidgetSnapshot() {
        val preferences = fetchUserPreferencesUseCase()
        val smokeCounts = fetchSmokeCountListUseCase(
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val goalSmokes = fetchSmokesUseCase(start = goalDataFetchStart(preferences))
        val goalProgress = evaluateGoalProgressUseCase(preferences.activeGoal, goalSmokes, preferences)
        widgetRefreshService.refreshHomeSnapshot(smokeCounts.toWidgetSnapshot(preferences, goalProgress))
    }

    private suspend fun refreshWidgetSnapshotBestEffort() {
        runCatching { refreshWidgetSnapshot() }
            .onFailure { Timber.w(it, "Home mutation succeeded but widget refresh failed: ${it.debugSummary()}") }
    }

    private suspend fun syncWithWearBestEffort() {
        runCatching { syncWithWearUseCase.invoke() }
            .onFailure { Timber.w(it, "Home mutation succeeded but Wear sync failed: ${it.debugSummary()}") }
    }
}

private fun personalizeGreetingTitle(
    greetingTitle: String,
    displayName: String?,
): String {
    val firstName = displayName
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.substringBefore(" ")
        ?.takeIf { it.isNotBlank() }

    return if (firstName != null) "$greetingTitle $firstName" else greetingTitle
}
