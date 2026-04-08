package com.feragusper.smokeanalytics.features.history.presentation.process

import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.domain.toWidgetSnapshot
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentBucketDate
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetRefreshService
import com.feragusper.smokeanalytics.libraries.architecture.domain.dayBucketDate
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.catchAndLog
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.SyncWithWearUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toDeprecatedInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Handles the business logic of processing intents for the History feature.
 *
 * This class is responsible for transforming [HistoryIntent] into [HistoryResult]s,
 * encapsulating the application's business logic for managing smoke history.
 *
 * @property addSmokeUseCase Use case for adding a smoke event.
 * @property editSmokeUseCase Use case for editing a smoke event.
 * @property deleteSmokeUseCase Use case for deleting a smoke event.
 * @property fetchSmokesUseCase Use case for fetching smoke events.
 * @property fetchSessionUseCase Use case for fetching the current session information.
 */
class HistoryProcessHolder @Inject constructor(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val editSmokeUseCase: EditSmokeUseCase,
    private val deleteSmokeUseCase: DeleteSmokeUseCase,
    private val fetchSmokesUseCase: FetchSmokesUseCase,
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val syncWithWearUseCase: SyncWithWearUseCase,
    private val fetchSmokeCountListUseCase: FetchSmokeCountListUseCase,
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
    private val locationCaptureService: LocationCaptureService,
    private val widgetRefreshService: WidgetRefreshService,
) : MVIProcessHolder<HistoryIntent, HistoryResult> {

    /**
     * Processes an [HistoryIntent] and transforms it into a stream of [HistoryResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A [Flow] emitting the corresponding [HistoryResult]s.
     */
    override fun processIntent(intent: HistoryIntent): Flow<HistoryResult> = when (intent) {
        is HistoryIntent.FetchSmokes -> processFetchSmokes(intent)
        is HistoryIntent.AddSmoke -> processAddSmoke(intent)
        is HistoryIntent.EditSmoke -> processEditSmoke(intent)
        is HistoryIntent.DeleteSmoke -> processDeleteSmoke(intent)
        HistoryIntent.NavigateUp -> flow { emit(HistoryResult.NavigateUp) }
    }

    /**
     * Handles the [HistoryIntent.DeleteSmoke] intent by deleting the specified smoke event.
     *
     * @param intent The delete smoke intent containing the smoke ID to be deleted.
     * @return A [Flow] emitting the result of the delete operation.
     */
    private fun processDeleteSmoke(intent: HistoryIntent.DeleteSmoke) = flow {
        emit(HistoryResult.DeleteSmokeInFlight(intent.id))
        deleteSmokeUseCase.invoke(intent.id)
        refreshWidgetSnapshot()
        emit(HistoryResult.DeleteSmokeSuccess)
        syncWithWearUseCase.invoke()
    }.catchAndLog {
        emit(HistoryResult.Error.Generic)
    }

    /**
     * Handles the [HistoryIntent.EditSmoke] intent by editing the specified smoke event.
     *
     * @param intent The edit smoke intent containing the smoke ID and new date.
     * @return A [Flow] emitting the result of the edit operation.
     */
    private fun processEditSmoke(intent: HistoryIntent.EditSmoke) = flow {
        emit(HistoryResult.EditSmokeInFlight(intent.id))
        editSmokeUseCase.invoke(intent.id, intent.date)
        refreshWidgetSnapshot()
        emit(HistoryResult.EditSmokeSuccess)
        syncWithWearUseCase.invoke()
    }.catchAndLog {
        emit(HistoryResult.Error.Generic)
    }

    /**
     * Handles the [HistoryIntent.FetchSmokes] intent by fetching the list of smoke events
     * for the specified date. It checks the user's session state before fetching.
     *
     * @param intent The fetch smoke intent containing the selected date.
     * @return A [Flow] emitting the result of the fetch operation.
     */
    private fun processFetchSmokes(intent: HistoryIntent.FetchSmokes) = flow {
        val preferences = runCatching { fetchUserPreferencesUseCase() }.getOrDefault(UserPreferences())
        val timeZone = TimeZone.currentSystemDefault()
        val selectedDate = intent.date.toLocalDateTime(timeZone).date
        val currentBucketDate = currentBucketDate(
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val dayStart = if (selectedDate == currentBucketDate) {
            currentDayStartInstant(
                timeZone = timeZone,
                dayStartHour = preferences.dayStartHour,
                manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
            )
        } else {
            selectedDate.atStartOfDayIn(timeZone)
                .plus(preferences.dayStartHour, DateTimeUnit.HOUR, timeZone)
                .toDeprecatedInstant()
        }
        val nextDayStart = if (selectedDate == currentBucketDate) {
            nextDayStartInstant(
                timeZone = timeZone,
                dayStartHour = preferences.dayStartHour,
                manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
            )
        } else {
            dayStart.plus(1, DateTimeUnit.DAY, timeZone)
        }
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> emit(HistoryResult.NotLoggedIn(dayStart))
            is Session.LoggedIn -> {
                emit(HistoryResult.Loading)
                val selectedBucketDate = dayStart.dayBucketDate(
                    timeZone = timeZone,
                    dayStartHour = preferences.dayStartHour,
                    manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                )
                val monthStart = LocalDate(
                    year = selectedBucketDate.year,
                    monthNumber = selectedBucketDate.monthNumber,
                    dayOfMonth = 1,
                ).atStartOfDayIn(timeZone)
                    .plus(preferences.dayStartHour, DateTimeUnit.HOUR, timeZone)
                    .toDeprecatedInstant()
                val nextMonthStart = LocalDate(
                    year = selectedBucketDate.year,
                    monthNumber = selectedBucketDate.monthNumber,
                    dayOfMonth = 1,
                ).plus(DatePeriod(months = 1)).atStartOfDayIn(timeZone)
                    .plus(preferences.dayStartHour, DateTimeUnit.HOUR, timeZone)
                    .toDeprecatedInstant()
                val monthCounts = fetchSmokesUseCase.invoke(monthStart, nextMonthStart)
                    .groupingBy { smoke ->
                        smoke.date.dayBucketDate(
                            timeZone = timeZone,
                            dayStartHour = preferences.dayStartHour,
                            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                        ).dayOfMonth
                    }
                    .eachCount()
                val previousMonthStart = LocalDate(
                    year = selectedBucketDate.year,
                    monthNumber = selectedBucketDate.monthNumber,
                    dayOfMonth = 1,
                ).plus(DatePeriod(months = -1)).atStartOfDayIn(timeZone)
                    .plus(preferences.dayStartHour, DateTimeUnit.HOUR, timeZone)
                    .toDeprecatedInstant()
                val previousMonthCounts = fetchSmokesUseCase.invoke(previousMonthStart, monthStart)
                    .groupingBy { smoke ->
                        smoke.date.dayBucketDate(
                            timeZone = timeZone,
                            dayStartHour = preferences.dayStartHour,
                            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                        ).dayOfMonth
                    }
                    .eachCount()
                emit(
                    HistoryResult.FetchSmokesSuccess(
                        selectedDate = dayStart,
                        smokes = fetchSmokesUseCase.invoke(dayStart, nextDayStart),
                        monthCounts = monthCounts,
                        previousMonthCounts = previousMonthCounts,
                    )
                )
            }
        }
    }.catchAndLog {
        emit(HistoryResult.FetchSmokesError)
    }

    /**
     * Handles the [HistoryIntent.AddSmoke] intent by adding a new smoke event for the specified date.
     * It checks the user's session state before adding the smoke.
     *
     * @param intent The add smoke intent containing the date of the smoke event.
     * @return A [Flow] emitting the result of the add operation.
     */
    private fun processAddSmoke(intent: HistoryIntent.AddSmoke): Flow<HistoryResult> = flow {
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> {
                emit(HistoryResult.Error.NotLoggedIn)
                emit(HistoryResult.GoToAuthentication)
            }

            is Session.LoggedIn -> {
                emit(HistoryResult.Loading)
                val preferences = runCatching { fetchUserPreferencesUseCase() }.getOrDefault(UserPreferences())
                val location = if (preferences.locationTrackingEnabled) {
                    locationCaptureService.captureCurrentLocation()?.let {
                        GeoPoint(latitude = it.latitude, longitude = it.longitude)
                    }
                } else {
                    null
                }
                addSmokeUseCase.invoke(intent.date, location)
                refreshWidgetSnapshot()
                emit(HistoryResult.AddSmokeSuccess)
                syncWithWearUseCase.invoke()
            }
        }
    }.catchAndLog {
        emit(HistoryResult.Error.Generic)
    }

    private suspend fun refreshWidgetSnapshot() {
        val preferences = runCatching { fetchUserPreferencesUseCase() }.getOrDefault(UserPreferences())
        val smokeCounts = fetchSmokeCountListUseCase(
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        widgetRefreshService.refreshHomeSnapshot(smokeCounts.toWidgetSnapshot(preferences))
    }
}
