package com.feragusper.smokeanalytics.features.stats.presentation.web

import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase

sealed interface StatsIntent {
    data class LoadStats(
        val year: Int,
        val month: Int,
        val day: Int,
        val period: FetchSmokeStatsUseCase.PeriodType,
    ) : StatsIntent
}