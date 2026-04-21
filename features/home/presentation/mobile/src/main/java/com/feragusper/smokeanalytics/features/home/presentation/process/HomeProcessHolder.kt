package com.feragusper.smokeanalytics.features.home.presentation.process

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.domain.financialSummary
import com.feragusper.smokeanalytics.features.home.domain.gamificationSummary
import com.feragusper.smokeanalytics.features.home.domain.greetingStateFor
import com.feragusper.smokeanalytics.features.home.domain.rateSummary
import com.feragusper.smokeanalytics.features.home.domain.toWidgetSnapshot
import com.feragusper.smokeanalytics.features.goals.domain.EvaluateGoalProgressUseCase
import com.feragusper.smokeanalytics.features.goals.domain.goalDataFetchStart
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.shouldOfferStartNewDay
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetRefreshService
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.catchAndLog
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
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import javax.inject.Inject

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
class HomeProcessHolder @Inject constructor(
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
    private val evaluateGoalProgressUseCase: EvaluateGoalProgressUseCase = EvaluateGoalProgressUseCase(),
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
                val smokeCounts = fetchSmokeCountListUseCase.invoke(
                    dayStartHour = preferences.dayStartHour,
                    manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                )
                val goalSmokes = fetchSmokesUseCase(start = goalDataFetchStart(preferences))
                val greetingState = greetingStateFor(
                    hourOfDay = kotlinx.datetime.Clock.System.now()
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
                        goalProgress = evaluateGoalProgressUseCase(preferences.activeGoal, goalSmokes, preferences),
                        canStartNewDay = shouldOfferStartNewDay(
                            dayStartHour = preferences.dayStartHour,
                            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                        ),
                    )
                )
                widgetRefreshService.refreshHomeSnapshot(smokeCounts.toWidgetSnapshot(preferences))
            }
        }
    }.catchAndLog {
        emit(HomeResult.FetchSmokesError)
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
    }.catchAndLog {
        emit(HomeResult.Error.Generic)
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
    }.catchAndLog {
        emit(HomeResult.Error.Generic)
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
                val location = if (preferences.locationTrackingEnabled) {
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
    }.catchAndLog {
        emit(HomeResult.Error.Generic)
    }

    private fun processStartNewDay(): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        val preferences = fetchUserPreferencesUseCase()
        updateUserPreferencesUseCase(
            preferences.copy(manualDayStartEpochMillis = kotlinx.datetime.Clock.System.now().toEpochMilliseconds())
        )
        emit(HomeResult.StartNewDaySuccess)
    }.catchAndLog {
        emit(HomeResult.Error.Generic)
    }

    private suspend fun refreshWidgetSnapshot() {
        val preferences = fetchUserPreferencesUseCase()
        val smokeCounts = fetchSmokeCountListUseCase(
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        widgetRefreshService.refreshHomeSnapshot(smokeCounts.toWidgetSnapshot(preferences))
    }

    private suspend fun refreshWidgetSnapshotBestEffort() {
        runCatching { refreshWidgetSnapshot() }
            .onFailure { Timber.w(it, "Home mutation succeeded but widget refresh failed") }
    }

    private suspend fun syncWithWearBestEffort() {
        runCatching { syncWithWearUseCase.invoke() }
            .onFailure { Timber.w(it, "Home mutation succeeded but Wear sync failed") }
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
