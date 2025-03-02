package com.feragusper.smokeanalytics.features.stats.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase.PeriodType

/**
 * Defines user intentions that can trigger actions within the Stats feature.
 *
 * This sealed class represents all possible user actions within the Stats module,
 * allowing the ViewModel to handle them in a structured manner.
 */
sealed class StatsIntent : MVIIntent {

    /**
     * Represents an intent to load smoking statistics.
     *
     * This is typically triggered when the Stats screen is opened or refreshed,
     * loading statistics data for the specified date and period type.
     *
     * @property year The year for which to load the statistics.
     * @property month The month (1-12) for which to load the statistics.
     * @property day The day (1-31) for which to load the statistics.
     * @property period The period type (Day, Week, Month, or Year) for which to load the statistics.
     */
    data class LoadStats(
        val year: Int,
        val month: Int,
        val day: Int,
        val period: PeriodType
    ) : StatsIntent()
}
