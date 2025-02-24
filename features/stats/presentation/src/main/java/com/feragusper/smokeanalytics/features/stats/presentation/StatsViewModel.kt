package com.feragusper.smokeanalytics.features.stats.presentation

import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsIntent
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsResult
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose.StatsViewState
import com.feragusper.smokeanalytics.features.stats.presentation.navigation.StatsNavigator
import com.feragusper.smokeanalytics.features.stats.presentation.process.StatsProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val processHolder: StatsProcessHolder,
) : MVIViewModel<StatsIntent, StatsViewState, StatsResult, StatsNavigator>(initialState = StatsViewState()) {

    override lateinit var navigator: StatsNavigator

    override suspend fun transformer(intent: StatsIntent) = processHolder.processIntent(intent)

    override suspend fun reducer(
        previous: StatsViewState,
        result: StatsResult,
    ): StatsViewState = when (result) {
        is StatsResult.Loading -> previous.copy(stats = null)
        is StatsResult.Success -> previous.copy(stats = result.stats)
        is StatsResult.Error -> previous.copy(stats = null)
    }
}
