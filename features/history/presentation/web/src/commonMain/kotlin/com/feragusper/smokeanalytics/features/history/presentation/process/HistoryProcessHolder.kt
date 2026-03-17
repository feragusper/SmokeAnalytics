package com.feragusper.smokeanalytics.features.history.presentation.process

import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentBucketDate
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.dayBucketDate
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextDayStartInstant
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class HistoryProcessHolder(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val editSmokeUseCase: EditSmokeUseCase,
    private val deleteSmokeUseCase: DeleteSmokeUseCase,
    private val fetchSmokesUseCase: FetchSmokesUseCase,
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
    private val locationCaptureService: LocationCaptureService,
) {

    fun processIntent(intent: HistoryIntent): Flow<HistoryResult> = when (intent) {
        is HistoryIntent.FetchSmokes -> processFetchSmokes(intent)
        is HistoryIntent.AddSmoke -> processAddSmoke(intent)
        is HistoryIntent.EditSmoke -> processEditSmoke(intent)
        is HistoryIntent.DeleteSmoke -> processDeleteSmoke(intent)
        HistoryIntent.NavigateUp -> flow { emit(HistoryResult.NavigateUp) }
    }

    private fun processFetchSmokes(intent: HistoryIntent.FetchSmokes) = flow {
        val tz = TimeZone.Companion.currentSystemDefault()
        val preferences = runCatching { fetchUserPreferencesUseCase() }.getOrDefault(UserPreferences())
        val selectedDate = intent.date.toLocalDateTime(tz).date
        val currentBucketDate = currentBucketDate(
            timeZone = tz,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val dayStart = if (selectedDate == currentBucketDate) {
            currentDayStartInstant(
                timeZone = tz,
                dayStartHour = preferences.dayStartHour,
                manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
            )
        } else {
            selectedDate.atStartOfDayIn(tz).plus(preferences.dayStartHour, DateTimeUnit.HOUR, tz)
        }
        val nextDayStart = if (selectedDate == currentBucketDate) {
            nextDayStartInstant(
                timeZone = tz,
                dayStartHour = preferences.dayStartHour,
                manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
            )
        } else {
            dayStart.plus(1, DateTimeUnit.Companion.DAY, tz)
        }

        when (fetchSessionUseCase()) {
            is Session.Anonymous -> emit(HistoryResult.NotLoggedIn(dayStart))

            is Session.LoggedIn -> {
                emit(HistoryResult.Loading)

                val filtered = fetchSmokesUseCase(
                    start = dayStart,
                    end = nextDayStart,
                )
                val selectedBucketDate = dayStart.dayBucketDate(
                    timeZone = tz,
                    dayStartHour = preferences.dayStartHour,
                    manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                )
                val monthStart = LocalDate(
                    year = selectedBucketDate.year,
                    monthNumber = selectedBucketDate.monthNumber,
                    dayOfMonth = 1,
                ).atStartOfDayIn(tz).plus(preferences.dayStartHour, DateTimeUnit.HOUR, tz)
                val nextMonthStart = LocalDate(
                    year = selectedBucketDate.year,
                    monthNumber = selectedBucketDate.monthNumber,
                    dayOfMonth = 1,
                ).plus(DatePeriod(months = 1)).atStartOfDayIn(tz).plus(preferences.dayStartHour, DateTimeUnit.HOUR, tz)
                val monthCounts = fetchSmokesUseCase(
                    start = monthStart,
                    end = nextMonthStart,
                ).groupingBy { smoke ->
                    smoke.date.dayBucketDate(
                        timeZone = tz,
                        dayStartHour = preferences.dayStartHour,
                        manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                    ).dayOfMonth
                }.eachCount()

                emit(
                    HistoryResult.FetchSmokesSuccess(
                        selectedDate = dayStart,
                        smokes = filtered,
                        monthCounts = monthCounts,
                    )
                )
            }
        }
    }.catch { emit(HistoryResult.FetchSmokesError) }

    private fun processAddSmoke(intent: HistoryIntent.AddSmoke) = flow {
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
                addSmokeUseCase(intent.date, location)
                emit(HistoryResult.AddSmokeSuccess)
            }
        }
    }.catch { emit(HistoryResult.Error.Generic) }

    private fun processEditSmoke(intent: HistoryIntent.EditSmoke) = flow {
        emit(HistoryResult.Loading)
        editSmokeUseCase(intent.id, intent.date)
        emit(HistoryResult.EditSmokeSuccess)
    }.catch { emit(HistoryResult.Error.Generic) }

    private fun processDeleteSmoke(intent: HistoryIntent.DeleteSmoke) = flow {
        emit(HistoryResult.Loading)
        deleteSmokeUseCase(intent.id)
        emit(HistoryResult.DeleteSmokeSuccess)
    }.catch { emit(HistoryResult.Error.Generic) }
}
