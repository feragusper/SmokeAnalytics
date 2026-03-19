package com.feragusper.smokeanalytics.features.home.presentation.web.process

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.domain.financialSummary
import com.feragusper.smokeanalytics.features.home.domain.gamificationSummary
import com.feragusper.smokeanalytics.features.home.domain.greetingStateFor
import com.feragusper.smokeanalytics.features.home.domain.rateSummary
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService
import com.feragusper.smokeanalytics.libraries.architecture.domain.shouldOfferStartNewDay
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.logging.AppLogger
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.toLocalDateTime

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
    private val fetchSmokeCountListUseCase: FetchSmokeCountListUseCase,
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    private val locationCaptureService: LocationCaptureService,
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
        is HomeIntent.TickTimeSinceLastCigarette -> flow {
            emit(
                HomeResult.UpdateTimeSinceLastCigarette(
                    timeSinceLastCigarette = intent.lastCigarette?.date?.timeElapsedSinceNow() ?: (0L to 0L),
                    lastSmoke = intent.lastCigarette,
                )
            )
        }
    }

    private fun processFetchSmokes(isRefresh: Boolean): Flow<HomeResult> = flow {
        repeat(5) { attempt ->
            when (fetchSessionUseCase()) {
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
                    val preferences = runCatching { fetchUserPreferencesUseCase() }.getOrDefault(UserPreferences())
                    val smokeCounts = fetchSmokeCountListUseCase(
                        dayStartHour = preferences.dayStartHour,
                        manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                    )
                    emit(
                        HomeResult.FetchSmokesSuccess(
                            smokeCountListResult = smokeCounts,
                            preferences = preferences,
                            greetingState = greetingStateFor(
                                hourOfDay = kotlinx.datetime.Clock.System.now()
                                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).hour,
                                todayCount = smokeCounts.countByToday,
                                currentStreakHours = smokeCounts.timeSinceLastCigarette.first,
                            ),
                            financialSummary = financialSummary(
                                todayCount = smokeCounts.countByToday,
                                weekCount = smokeCounts.countByWeek,
                                monthCount = smokeCounts.countByMonth,
                                preferences = preferences,
                            ),
                            rateSummary = rateSummary(smokeCounts),
                            gamificationSummary = gamificationSummary(smokeCounts.todaysSmokes),
                            canStartNewDay = shouldOfferStartNewDay(
                                dayStartHour = preferences.dayStartHour,
                                manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
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
                val preferences = runCatching { fetchUserPreferencesUseCase() }.getOrDefault(UserPreferences())
                val location = if (preferences.locationTrackingEnabled) {
                    locationCaptureService.captureCurrentLocation()?.let {
                        GeoPoint(latitude = it.latitude, longitude = it.longitude)
                    }
                } else {
                    null
                }
                addSmokeUseCase(location = location)
                AppLogger.i { "Smoke added successfully" }
                emit(HomeResult.AddSmokeSuccess)
            }
        }
    }.catch { e ->
        AppLogger.e { "Error adding smoke: ${e.message}" }
        emit(HomeResult.Error.Generic)
    }

    private fun processStartNewDay(): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        val preferences = runCatching { fetchUserPreferencesUseCase() }.getOrDefault(UserPreferences())
        updateUserPreferencesUseCase(
            preferences.copy(manualDayStartEpochMillis = kotlinx.datetime.Clock.System.now().toEpochMilliseconds())
        )
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
}
