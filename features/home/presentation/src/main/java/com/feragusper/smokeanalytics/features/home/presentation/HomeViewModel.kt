package com.feragusper.smokeanalytics.features.home.presentation

import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.AddSmokeSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.DeleteSmokeSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.EditSmokeSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.Error
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.FetchSmokesError
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.FetchSmokesSuccess
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.GoToAuthentication
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.GoToHistory
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.Loading
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.NotLoggedIn
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult.UpdateTimeSinceLastCigarette
import com.feragusper.smokeanalytics.features.home.presentation.mvi.compose.HomeViewState
import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator
import com.feragusper.smokeanalytics.features.home.presentation.process.HomeProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Timer
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

/**
 * ViewModel for the Home feature, responsible for processing user intents, interacting with the domain layer,
 * and updating the UI state.
 *
 * It extends [MVIViewModel] to implement the Model-View-Intent (MVI) architecture pattern.
 * This ViewModel handles home screen-related logic and updates the UI state accordingly.
 *
 * @property processHolder Responsible for processing intents and invoking use cases.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val processHolder: HomeProcessHolder,
) : MVIViewModel<HomeIntent, HomeViewState, HomeResult, HomeNavigator>(
    initialState = HomeViewState()
) {

    /**
     * Timer for updating the time since the last cigarette.
     */
    private var timer: Timer? = null

    /**
     * Navigator instance for handling navigation actions.
     */
    override lateinit var navigator: HomeNavigator

    init {
        // Trigger initial intent to fetch smoke events.
        intents().trySend(HomeIntent.FetchSmokes)
    }

    /**
     * Transforms [HomeIntent] into a stream of [HomeResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A Flow of [HomeResult] representing the result of processing the intent.
     */
    override fun transformer(intent: HomeIntent) = processHolder.processIntent(intent)

    /**
     * Reduces the previous [HomeViewState] and a new [HomeResult] to a new state.
     *
     * This function is responsible for creating the new state based on the current state and the result.
     *
     * @param previous The previous state of the UI.
     * @param result The result of processing the intent.
     * @return The new state of the UI.
     */
    override suspend fun reducer(previous: HomeViewState, result: HomeResult): HomeViewState =
        when (result) {
            Loading -> previous.copy(
                displayLoading = true,
                displayRefreshLoading = false,
                error = null,
            )

            HomeResult.RefreshLoading -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = null,
            )

            NotLoggedIn -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = null,
                smokesPerDay = 0,
                smokesPerWeek = 0,
                smokesPerMonth = 0,
                timeSinceLastCigarette = 0L to 0L,
            )

            GoToHistory -> {
                navigator.navigateToHistory()
                previous
            }

            GoToAuthentication -> {
                navigator.navigateToAuthentication()
                previous
            }

            is FetchSmokesSuccess -> {
                // Cancel existing timer and create a new one to update the time since the last cigarette
                timer?.cancel()
                timer = fixedRateTimer(
                    name = "timer",
                    period = TimeUnit.MINUTES.toMillis(1),
                ) {
                    intents().trySend(HomeIntent.TickTimeSinceLastCigarette(result.smokeCountListResult.lastSmoke))
                }

                previous.copy(
                    displayLoading = false,
                    displayRefreshLoading = false,
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
                // Re-fetch smokes when adding, editing, or deleting a smoke.
                intents().trySend(HomeIntent.FetchSmokes)
                previous
            }

            is Error -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = result,
            )

            FetchSmokesError -> previous.copy(
                displayLoading = false,
                displayRefreshLoading = false,
                error = Error.Generic,
            )
        }
}
