package com.feragusper.smokeanalytics.features.stats.presentation.web

import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase

class StatsWebDependencies(
    val processHolder: StatsProcessHolder,
)

fun createStatsWebDependencies(
    fetchSmokeStatsUseCase: FetchSmokeStatsUseCase,
): StatsWebDependencies {
    return StatsWebDependencies(
        processHolder = StatsProcessHolder(fetchSmokeStatsUseCase),
    )
}