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
 *
 * This class is responsible for transforming [StatsIntent] into [StatsResult]s,
 * encapsulating the application's business logic for retrieving statistics data.
 *
 * @property fetchSmokeStatsUseCase Use case for fetching smoke statistics.
 */
class StatsProcessHolder @Inject constructor(
    private val fetchSmokeStatsUseCase: FetchSmokeStatsUseCase
) : MVIProcessHolder<StatsIntent, StatsResult> {

    /**
     * Processes a [StatsIntent] and transforms it into a stream of [StatsResult]s.
     *
     * This method handles different intents related to statistics,
     * fetching the relevant data and emitting the corresponding results.
     *
     * @param intent The user intent to be processed.
     * @return A [Flow] emitting the corresponding [StatsResult]s.
     */
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

    /**
     * Fetches smoke statistics for the specified date and period type.
     *
     * This method interacts with the domain layer to retrieve the statistics data,
     * emitting loading, success, or error states accordingly.
     *
     * @param year The year for which to fetch the statistics.
     * @param month The month (1-12) for which to fetch the statistics.
     * @param day The day (1-31) for which to fetch the statistics.
     * @param period The period type (Day, Week, Month, or Year) for which to fetch the statistics.
     * @return A [Flow] emitting [StatsResult]s representing the loading, success, or error states.
     */
    private fun fetchStats(
        year: Int,
        month: Int,
        day: Int,
        period: FetchSmokeStatsUseCase.PeriodType
    ): Flow<StatsResult> = flow {
        // Emit loading state to show a loading indicator in the UI
        emit(StatsResult.Loading)

        // Fetch the statistics data using the use case
        val stats = fetchSmokeStatsUseCase(year, month, day, period)

        // Emit the success result with the fetched statistics
        emit(StatsResult.Success(stats))
    }.catchAndLog { e ->
        // Log the error and emit an error state
        emit(StatsResult.Error(e))
    }
}
