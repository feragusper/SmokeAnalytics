package com.feragusper.smokeanalytics.features.stats.presentation.web

import com.feragusper.smokeanalytics.features.stats.presentation.web.process.StatsProcessHolder
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase

/**
 * Represents the dependencies for the Stats screen.
 *
 * @property processHolder The process holder for the Stats screen.
 */
class StatsWebDependencies(
    val processHolder: StatsProcessHolder,
)

/**
 * Creates the dependencies for the Stats screen.
 *
 * @param fetchSmokeStatsUseCase The use case for fetching the smoke stats.
 *
 * @return The dependencies for the Stats screen.
 */
fun createStatsWebDependencies(
    fetchSmokeStatsUseCase: FetchSmokeStatsUseCase,
): StatsWebDependencies {
    return StatsWebDependencies(
        processHolder = StatsProcessHolder(fetchSmokeStatsUseCase),
    )
}