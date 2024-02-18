package com.feragusper.smokeanalytics.features.home.presentation

import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.AddSmokeSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.DeleteSmokeSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.EditSmokeSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.Error
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.FetchSmokesError
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.FetchSmokesSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.GoToLogin
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.Loading
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.NotLoggedIn
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.UpdateTimeSinceLastCigarette
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeViewState
import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator
import com.feragusper.smokeanalytics.features.home.presentation.process.HomeProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Timer
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val processHolder: HomeProcessHolder,
) : MVIViewModel<HomeIntent, HomeViewState, HomeResult, HomeNavigator>(initialState = HomeViewState()) {

    private var timer: Timer? = null
    override lateinit var navigator: HomeNavigator

    init {
        intents().trySend(HomeIntent.FetchSmokes)
    }

    override suspend fun transformer(intent: HomeIntent) = processHolder.processIntent(intent)

    override suspend fun reducer(previous: HomeViewState, result: HomeResult): HomeViewState =
        when (result) {
            Loading -> previous.copy(
                displayLoading = true,
                error = null,
            )

            NotLoggedIn -> previous.copy(
                displayLoading = false,
                error = null,
                smokesPerDay = 0,
                smokesPerWeek = 0,
                smokesPerMonth = 0,
                timeSinceLastCigarette = 0L to 0L,
            )

            GoToLogin -> {
                navigator.navigateToSettings()
                previous
            }

            is FetchSmokesSuccess -> {
                timer?.cancel()
                timer = fixedRateTimer(
                    name = "timer",
                    period = TimeUnit.MINUTES.toMillis(1),
                ) {
                    intents().trySend(HomeIntent.TickTimeSinceLastCigarette(result.smokeCountListResult.lastSmoke))
                }

                previous.copy(
                    displayLoading = false,
                    error = null,
                    smokesPerDay = result.smokeCountListResult.countByToday,
                    smokesPerWeek = result.smokeCountListResult.countByWeek,
                    smokesPerMonth = result.smokeCountListResult.countByMonth,
                    latestSmokes = result.smokeCountListResult.todaysSmokes,
                    timeSinceLastCigarette = result.smokeCountListResult.timeSinceLastCigarette,
                )
            }

            is UpdateTimeSinceLastCigarette -> {
                previous.copy(
                    timeSinceLastCigarette = result.timeSinceLastCigarette
                )
            }

            DeleteSmokeSuccess, EditSmokeSuccess, AddSmokeSuccess -> {
                intents().trySend(HomeIntent.FetchSmokes)
                previous
            }

            is Error -> previous.copy(
                displayLoading = false,
                error = result,
            )

            FetchSmokesError -> previous.copy(
                displayLoading = false,
                error = Error.Generic,
            )
        }
}
