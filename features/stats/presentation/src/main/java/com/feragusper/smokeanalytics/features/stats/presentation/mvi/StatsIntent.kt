package com.feragusper.smokeanalytics.features.stats.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase.PeriodType

/**
 * Defines user intentions that can trigger actions within the Stats feature.
 * Currently, this sealed class doesn't contain any actions, serving as a placeholder
 * for future expansion.
 */
sealed class StatsIntent : MVIIntent {
    data class LoadStats(val year: Int, val month: Int, val day: Int, val period: PeriodType) : StatsIntent()
}
