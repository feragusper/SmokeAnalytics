package com.feragusper.smokeanalytics.features.home.presentation.presentation

import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeViewState
import com.feragusper.smokeanalytics.features.home.presentation.presentation.process.HomeProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

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
                smokeAddError = null,
            )

            HomeResult.NotLoggedIn -> previous.copy(
                displayLoading = false,
                displaySmokeAddedSuccess = false,
                smokeAddError = null,
            )

            HomeResult.GoToLogin -> {
                navigator.navigateToSettings()
                previous
            }

            is HomeResult.FetchSmokesSuccess -> previous.copy(
                displayLoading = false,
                displaySmokeAddedSuccess = true,
                smokeAddError = null,
                smokesPerDay = result.smokeCountListResult.countByToday,
                smokesPerWeek = result.smokeCountListResult.countByWeek,
                smokesPerMonth = result.smokeCountListResult.countByMonth,
                latestSmokes = result.smokeCountListResult.todaysSmokes,
                timeSinceLastCigarette = result.smokeCountListResult.timeSinceLastCigarette
            )

            HomeResult.AddSmokeSuccess -> {
                intents().trySend(HomeIntent.FetchSmokes)
                previous
            }

            is HomeResult.AddSmokeError -> previous.copy(
                displayLoading = false,
                displaySmokeAddedSuccess = false,
                smokeAddError = result,
            )

            HomeResult.FetchSmokesError -> previous.copy(
                displayLoading = false,
                displaySmokeAddedSuccess = false,
            )
        }
}
