package com.feragusper.smokeanalytics.features.stats.presentation.process

import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsIntent
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.catchAndLog
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Processes intents from the Stats feature, fetching and transforming
 * smoking statistics into a usable format for the UI.
 */
class StatsProcessHolder @Inject constructor(
    private val fetchSmokeStatsUseCase: FetchSmokeStatsUseCase
) : MVIProcessHolder<StatsIntent, StatsResult> {

    override fun processIntent(intent: StatsIntent): Flow<StatsResult> {
        return when (intent) {
            is StatsIntent.LoadStats -> fetchStats(
                intent.year,
                intent.month,
                intent.day,
                intent.period
            )
        }
    }

    private fun fetchStats(
        year: Int,
        month: Int,
        day: Int,
        period: FetchSmokeStatsUseCase.PeriodType
    ): Flow<StatsResult> = flow {
        emit(StatsResult.Loading)
        val stats = fetchSmokeStatsUseCase(year, month, day, period)
        emit(StatsResult.Success(stats))
    }.catchAndLog { e ->
        emit(StatsResult.Error(e))
    }
}
