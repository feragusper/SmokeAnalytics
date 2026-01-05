package com.feragusper.smokeanalytics.features.stats.presentation.web.process

import com.feragusper.smokeanalytics.features.stats.presentation.web.mvi.StatsIntent
import com.feragusper.smokeanalytics.features.stats.presentation.web.mvi.StatsResult
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class StatsProcessHolder(
    private val fetchSmokeStatsUseCase: FetchSmokeStatsUseCase,
) {

    fun processIntent(intent: StatsIntent): Flow<StatsResult> = when (intent) {
        is StatsIntent.LoadStats -> processLoadStats(intent)
    }

    private fun processLoadStats(intent: StatsIntent.LoadStats): Flow<StatsResult> = flow {
        emit(StatsResult.Loading)
        val stats = fetchSmokeStatsUseCase(
            year = intent.year,
            month = intent.month,
            day = intent.day,
            periodType = intent.period,
        )
        emit(StatsResult.Success(stats))
    }.catch { e ->
        emit(StatsResult.Error(e))
    }
}