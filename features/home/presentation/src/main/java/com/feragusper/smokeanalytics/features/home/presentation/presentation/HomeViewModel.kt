package com.feragusper.smokeanalytics.features.home.presentation.presentation

import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeViewState
import com.feragusper.smokeanalytics.features.home.presentation.presentation.process.HomeProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val processHolder: HomeProcessHolder,
) : MVIViewModel<HomeIntent, HomeViewState, HomeResult, HomeNavigator>(initialState = HomeViewState()) {

    override lateinit var navigator: HomeNavigator

    init {
        intents().trySend(HomeIntent.FetchSmokes)
    }

    override suspend fun transformer(intent: HomeIntent) = processHolder.processIntent(intent)

    override suspend fun reducer(previous: HomeViewState, result: HomeResult): HomeViewState =
        when (result) {
            HomeResult.Loading -> previous.copy(
                displayLoading = true,
                displaySmokeAddedSuccess = false,
                error = null,
            )

            HomeResult.NotLoggedIn -> previous.copy(
                displayLoading = false,
                displaySmokeAddedSuccess = false,
                error = null,
                smokesPerDay = 0,
                smokesPerWeek = 0,
                smokesPerMonth = 0,
                timeSinceLastCigarette = 0L to 0L,
            )

            HomeResult.GoToLogin -> {
                navigator.navigateToSettings()
                previous
            }

            is HomeResult.FetchSmokesSuccess -> {
                fixedRateTimer(
                    name = "timer",
                    period = TimeUnit.MINUTES.toMillis(1),
                ) {
                    intents().trySend(HomeIntent.TickTimeSinceLastCigarette(result.smokeCountListResult.lastCigarette))
                }

                previous.copy(
                    displayLoading = false,
                    displaySmokeAddedSuccess = true,
                    error = null,
                    smokesPerDay = result.smokeCountListResult.countByToday,
                    smokesPerWeek = result.smokeCountListResult.countByWeek,
                    smokesPerMonth = result.smokeCountListResult.countByMonth,
                    latestSmokes = result.smokeCountListResult.todaysSmokes,
                    timeSinceLastCigarette = result.smokeCountListResult.timeSinceLastCigarette
                )
            }

            is HomeResult.UpdateTimeSinceLastCigarette -> {
                previous.copy(
                    timeSinceLastCigarette = result.timeSinceLastCigarette
                )
            }

            HomeResult.AddSmokeSuccess -> {
                intents().trySend(HomeIntent.FetchSmokes)
                previous
            }

            is HomeResult.Error -> previous.copy(
                displayLoading = false,
                displaySmokeAddedSuccess = false,
                error = result,
            )

            HomeResult.FetchSmokesError -> previous.copy(
                displayLoading = false,
                displaySmokeAddedSuccess = false,
                error = HomeResult.Error.Generic,
            )
        }
}
