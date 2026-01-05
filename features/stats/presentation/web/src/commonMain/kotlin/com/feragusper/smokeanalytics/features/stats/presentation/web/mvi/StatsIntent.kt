package com.feragusper.smokeanalytics.features.stats.presentation.web.mvi

import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase

/**
 * Represents the intents that can be sent to the Stats screen.
 */
sealed interface StatsIntent {

    /**
     * Represents the intent to load the stats.
     *
     * @property year The year of the stats.
     * @property month The month of the stats.
     * @property day The day of the stats.
     * @property period The period of the stats.
     */
    data class LoadStats(
        val year: Int,
        val month: Int,
        val day: Int,
        val period: FetchSmokeStatsUseCase.PeriodType,
    ) : StatsIntent
}