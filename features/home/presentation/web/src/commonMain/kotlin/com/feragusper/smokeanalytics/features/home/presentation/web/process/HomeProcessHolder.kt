package com.feragusper.smokeanalytics.features.home.presentation.web.process

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.domain.RelationshipTrackingSince
import com.feragusper.smokeanalytics.features.home.domain.financialSummary
import com.feragusper.smokeanalytics.features.home.domain.gamificationSummary
import com.feragusper.smokeanalytics.features.home.domain.greetingStateFor
import com.feragusper.smokeanalytics.features.home.domain.rateSummary
import com.feragusper.smokeanalytics.features.goals.domain.EvaluateGoalProgressUseCase
import com.feragusper.smokeanalytics.features.goals.domain.goalDataFetchStart
import com.feragusper.smokeanalytics.libraries.cravings.domain.CravingWaitCalculator
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.toCravingStats
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.AddCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.DeleteCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.FetchActiveCravingUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.FetchCravingsUseCase
import com.feragusper.smokeanalytics.libraries.cravings.domain.usecase.ResolveCravingUseCase
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsSource
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.shouldOfferStartNewDay
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.logging.AppLogger
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeRelationship
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.SetSmokeRelationshipUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/**
 * Represents the process holder for the Home screen.
 *
 * @property addSmokeUseCase The use case for adding a smoke.
 * @property editSmokeUseCase The use case for editing a smoke.
 * @property deleteSmokeUseCase The use case for deleting a smoke.
 * @property fetchSmokeCountListUseCase The use case for fetching smoke count list.
 * @property fetchSessionUseCase The use case for fetching the session.
 */
class HomeProcessHolder(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val editSmokeUseCase: EditSmokeUseCase,
    private val deleteSmokeUseCase: DeleteSmokeUseCase,
    private val setSmokeRelationshipUseCase: SetSmokeRelationshipUseCase,
    private val fetchSmokeCountListUseCase: FetchSmokeCountListUseCase,
    private val fetchSmokesUseCase: FetchSmokesUseCase,
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    private val locationCaptureService: LocationCaptureService,
    private val addCravingUseCase: AddCravingUseCase,
    private val deleteCravingUseCase: DeleteCravingUseCase,
    private val fetchActiveCravingUseCase: FetchActiveCravingUseCase,
    private val fetchCravingsUseCase: FetchCravingsUseCase,
    private val resolveCravingUseCase: ResolveCravingUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val evaluateGoalProgressUseCase: EvaluateGoalProgressUseCase = EvaluateGoalProgressUseCase(),
    private val cravingWaitCalculator: CravingWaitCalculator = CravingWaitCalculator(),
) {

    /**
     * Processes the given intent and returns a flow of results.
     *
     * @param intent The intent to process.
     * @return A flow of results.
     */
    fun processIntent(intent: HomeIntent): Flow<HomeResult> = when (intent) {
        HomeIntent.FetchSmokes -> processFetchSmokes(isRefresh = false)
        HomeIntent.RefreshFetchSmokes -> processFetchSmokes(isRefresh = true)
        HomeIntent.AddSmoke -> processAddSmoke()
        HomeIntent.StartNewDay -> processStartNewDay()
        is HomeIntent.EditSmoke -> processEditSmoke(intent)
        is HomeIntent.DeleteSmoke -> processDeleteSmoke(intent)
        HomeIntent.OnClickHistory -> flow { emit(HomeResult.GoToHistory) }
        HomeIntent.OnClickGoals -> flow { emit(HomeResult.GoToGoals) }
        is HomeIntent.TickTimeSinceLastCigarette -> flow {
            emit(
                HomeResult.UpdateTimeSinceLastCigarette(
                    timeSinceLastCigarette = intent.lastCigarette?.date?.timeElapsedSinceNow() ?: (0L to 0L),
                    lastSmoke = intent.lastCigarette,
                )
            )
        }
        HomeIntent.TrackCraving -> processTrackCraving()
        is HomeIntent.ResolveCraving -> processResolveCraving(intent)
        is HomeIntent.DismissCraving -> processDismissCraving(intent)
        HomeIntent.DismissCravingHint -> flow { emit(HomeResult.CravingHintDismissed) }
        HomeIntent.DismissCravingCelebration -> flow { emit(HomeResult.CravingCelebrationDismissed) }
        is HomeIntent.OpenRelationshipPrompt -> flow { emit(HomeResult.AddSmokeSuccess(intent.smokeId)) }
        is HomeIntent.SaveSmokeRelationship -> processSaveRelationship(intent)
        is HomeIntent.SkipSmokeRelationship -> processSkipRelationship(intent)
        HomeIntent.DismissRelationshipPrompt -> flow { emit(HomeResult.RelationshipPromptDismissed) }
    }

    private fun processSaveRelationship(intent: HomeIntent.SaveSmokeRelationship): Flow<HomeResult> = flow<HomeResult> {
        setSmokeRelationshipUseCase(
            id = intent.smokeId,
            relationship = SmokeRelationship.Tagged(tags = intent.tags),
        )
        persistNewCustomTags(intent.tags)
        analyticsTracker.relationshipTagged(intent.tags.size)
        emit(HomeResult.RelationshipUpdated)
    }.catch { emit(HomeResult.Error.Generic) }

    /**
     * Adds any ad-hoc tags the user typed (not a built-in default and not already saved) to the
     * custom trigger catalog so they're selectable in future prompts and in Settings.
     */
    private suspend fun persistNewCustomTags(tags: Set<String>) {
        val preferences = fetchUserPreferencesUseCase()
        val defaultKeys = SmokeTrigger.entries.map { it.key }.toSet()
        val newTags = tags.filter { tag ->
            tag !in defaultKeys && preferences.customTriggers.none { it.equals(tag, ignoreCase = true) }
        }
        if (newTags.isNotEmpty()) {
            updateUserPreferencesUseCase(
                preferences.copy(customTriggers = preferences.customTriggers + newTags),
            )
        }
    }

    private fun processSkipRelationship(intent: HomeIntent.SkipSmokeRelationship): Flow<HomeResult> = flow<HomeResult> {
        setSmokeRelationshipUseCase(id = intent.smokeId, relationship = SmokeRelationship.Skipped)
        analyticsTracker.relationshipSkipped()
        emit(HomeResult.RelationshipUpdated)
    }.catch { emit(HomeResult.Error.Generic) }

    private fun processFetchSmokes(isRefresh: Boolean): Flow<HomeResult> = flow {
        repeat(5) { attempt ->
            when (val session = fetchSessionUseCase()) {
                is Session.Anonymous -> {
                    if (!isRefresh && attempt < 4) {
                        if (attempt == 0) emit(HomeResult.Loading)
                        delay(300)
                        return@repeat
                    } else {
                        emit(HomeResult.NotLoggedIn)
                        return@flow
                    }
                }

                is Session.LoggedIn -> {
                    emit(if (isRefresh) HomeResult.RefreshLoading else HomeResult.Loading)
                    val preferences = fetchUserPreferencesUseCase()
                    val locationTrackingAvailability = locationCaptureService.locationTrackingAvailability(
                        preferences.locationTrackingEnabled
                    )
                    val smokeCounts = fetchSmokeCountListUseCase(
                        dayStartHour = preferences.dayStartHour,
                        manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                    )
                    val goalSmokes = fetchSmokesUseCase(start = goalDataFetchStart(preferences))
                    val pendingRelationshipSmokes = fetchSmokesUseCase(
                        start = Clock.System.now().minus(RELATIONSHIP_LOOKBACK_DAYS.days),
                        end = Clock.System.now(),
                    ).filter { it.relationship.isPending && it.date >= RelationshipTrackingSince }
                    val activeCraving = fetchActiveCravingUseCase()
                    val cravingStats = fetchCravingsUseCase(start = goalDataFetchStart(preferences)).toCravingStats()
                    val timeZone = TimeZone.currentSystemDefault()
                    val now = Clock.System.now()
                    val today = now.toLocalDateTime(timeZone).date
                    val currentMonthStart = LocalDate(today.year, today.monthNumber, 1)
                        .atStartOfDayIn(timeZone)
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
                    val previousMonthCount = fetchSmokesUseCase(
                        start = previousMonthStart,
                        end = previousMonthWindowEnd,
                    ).size
                    val greetingState = greetingStateFor(
                        hourOfDay = Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault()).hour,
                        todayCount = smokeCounts.countByToday,
                        currentStreakHours = smokeCounts.timeSinceLastCigarette.first,
                        nickname = preferences.nickname,
                    )
                    emit(
                        HomeResult.FetchSmokesSuccess(
                            smokeCountListResult = smokeCounts,
                            preferences = preferences,
                            greetingState = greetingState.copy(
                                name = personalizeGreetingName(
                                    nickname = greetingState.name,
                                    displayName = session.user.displayName,
                                ),
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
                            locationTrackingAvailability = locationTrackingAvailability,
                            previousMonthCount = previousMonthCount,
                            activeCraving = activeCraving,
                            cravingStats = cravingStats,
                            pendingRelationshipSmokes = pendingRelationshipSmokes,
                            availableTriggers = SmokeTrigger.catalog(
                                customTriggers = preferences.customTriggers,
                                hiddenDefaultKeys = preferences.hiddenDefaultTriggers,
                                iconOverrides = preferences.triggerIcons,
                                labelOverrides = preferences.triggerLabels,
                            ),
                        )
                    )
                    return@flow
                }
            }
        }
    }.catch {
        emit(HomeResult.FetchSmokesError)
    }

    private fun processAddSmoke(): Flow<HomeResult> = flow {
        AppLogger.d { "AddSmoke intent received" }

        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> {
                AppLogger.w { "AddSmoke blocked: user is anonymous" }
                emit(HomeResult.Error.NotLoggedIn)
                emit(HomeResult.GoToAuthentication)
            }

            is Session.LoggedIn -> {
                AppLogger.i { "User logged in, adding smoke..." }
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
                val smokeId = addSmokeUseCase(location = location)
                AppLogger.i { "Smoke added successfully" }
                analyticsTracker.smokeAdded(AnalyticsSource.HOME)
                emit(HomeResult.AddSmokeSuccess(smokeId))
            }
        }
    }.catch { e ->
        AppLogger.e { "Error adding smoke: ${e.message}" }
        emit(HomeResult.Error.Generic)
    }

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
                    analyticsTracker.cravingTracked()
                    emit(HomeResult.CravingTracked(craving))
                }
            }
        }
    }.catch {
        emit(HomeResult.Error.Generic)
    }

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
        analyticsTracker.cravingResolved(smoked = intent.smoked)
        emit(HomeResult.CravingResolved(outcome = outcome, points = points))
    }.catch {
        emit(HomeResult.Error.Generic)
    }

    private fun processDismissCraving(intent: HomeIntent.DismissCraving): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        deleteCravingUseCase(intent.craving.id)
        analyticsTracker.cravingDismissed()
        emit(HomeResult.CravingDismissed)
    }.catch {
        emit(HomeResult.Error.Generic)
    }

    private fun processStartNewDay(): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        val preferences = fetchUserPreferencesUseCase()
        updateUserPreferencesUseCase(
            preferences.copy(manualDayStartEpochMillis = Clock.System.now().toEpochMilliseconds())
        )
        analyticsTracker.newDayStarted()
        emit(HomeResult.StartNewDaySuccess)
    }.catch {
        emit(HomeResult.Error.Generic)
    }

    private fun processEditSmoke(intent: HomeIntent.EditSmoke): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        editSmokeUseCase(intent.id, intent.date)
        emit(HomeResult.EditSmokeSuccess)
    }.catch {
        emit(HomeResult.Error.Generic)
    }

    private fun processDeleteSmoke(intent: HomeIntent.DeleteSmoke): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        deleteSmokeUseCase(intent.id)
        emit(HomeResult.DeleteSmokeSuccess)
    }.catch {
        emit(HomeResult.Error.Generic)
    }

    private companion object {
        // Firestore can't query "field absent", so the pending list is computed in memory
        // over this lookback window.
        const val RELATIONSHIP_LOOKBACK_DAYS = 30
    }
}

/** Keeps an explicit nickname, otherwise falls back to the account's first name. */
private fun personalizeGreetingName(
    nickname: String,
    displayName: String?,
): String {
    if (nickname.isNotBlank()) return nickname
    return displayName
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.substringBefore(" ")
        ?.takeIf { it.isNotBlank() }
        .orEmpty()
}
