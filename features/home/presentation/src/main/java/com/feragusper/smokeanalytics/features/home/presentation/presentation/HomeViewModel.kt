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
            is HomeResult.Loading -> previous.copy(
                displayLoading = true,
                displaySmokeAddedSuccess = false,
                displaySmokeAddedError = false,
            )

            is HomeResult.FetchSmokesSuccess -> previous.copy(
                displayLoading = false,
                displaySmokeAddedSuccess = true,
                displaySmokeAddedError = false,
                smokesPerDay = result.smokeCountListResult.byToday,
                smokesPerWeek = result.smokeCountListResult.byWeek,
                smokesPerMonth = result.smokeCountListResult.byMonth,
            )

            HomeResult.AddSmokeSuccess -> previous.copy(
                displayLoading = false,
                displaySmokeAddedSuccess = true,
                displaySmokeAddedError = false,
                smokesPerDay = previous.smokesPerDay?.plus(1),
                smokesPerWeek = previous.smokesPerWeek?.plus(1),
                smokesPerMonth = previous.smokesPerMonth?.plus(1),
            )

            HomeResult.AddSmokeError -> previous.copy(
                displayLoading = false,
                displaySmokeAddedSuccess = false,
                displaySmokeAddedError = true,
            )

            HomeResult.FetchSmokesError -> previous.copy(
                displayLoading = false,
                displaySmokeAddedSuccess = false,
                displaySmokeAddedError = true,
            )
        }
}
