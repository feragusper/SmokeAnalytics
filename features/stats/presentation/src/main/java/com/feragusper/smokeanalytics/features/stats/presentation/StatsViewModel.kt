package com.feragusper.smokeanalytics.features.stats.presentation

import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsIntent
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsResult
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose.StatsViewState
import com.feragusper.smokeanalytics.features.stats.presentation.navigation.StatsNavigator
import com.feragusper.smokeanalytics.features.stats.presentation.process.StatsProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the Statistics feature, responsible for processing user intents,
 * interacting with the domain layer, and updating the UI state.
 *
 * It extends [MVIViewModel] to implement the Model-View-Intent (MVI) architecture pattern.
 * This ViewModel handles statistics screen-related logic and updates the UI state accordingly.
 *
 * @property processHolder Responsible for processing intents and invoking the corresponding actions.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val processHolder: StatsProcessHolder,
) : MVIViewModel<StatsIntent, StatsViewState, StatsResult, StatsNavigator>(
    initialState = StatsViewState()
) {

    /**
     * Navigator instance for handling navigation actions.
     */
    override lateinit var navigator: StatsNavigator

    /**
     * Transforms [StatsIntent] into a stream of [StatsResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A Flow of [StatsResult] representing the result of processing the intent.
     */
    override fun transformer(intent: StatsIntent) = processHolder.processIntent(intent)

    /**
     * Reduces the previous [StatsViewState] and a new [StatsResult] to a new state.
     *
     * This function is responsible for creating the new state based on the current state and the result.
     *
     * @param previous The previous state of the UI.
     * @param result The result of processing the intent.
     * @return The new state of the UI.
     */
    override suspend fun reducer(
        previous: StatsViewState,
        result: StatsResult,
    ): StatsViewState = when (result) {

        /**
         * Indicates that the statistics data is being loaded.
         *
         * This result is used to show a loading indicator while fetching the data.
         */
        is StatsResult.Loading -> previous.copy(stats = null)

        /**
         * Indicates a successful fetch of statistics data.
         *
         * This result is used to update the UI with the fetched statistics.
         */
        is StatsResult.Success -> previous.copy(stats = result.stats)

        /**
         * Indicates that an error occurred while fetching the statistics data.
         *
         * This result is used to display an error message or a fallback state.
         */
        is StatsResult.Error -> previous.copy(stats = null)
    }
}
