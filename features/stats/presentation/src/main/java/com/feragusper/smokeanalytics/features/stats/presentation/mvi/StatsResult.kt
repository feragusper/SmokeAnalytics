package com.feragusper.smokeanalytics.features.stats.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats

/**
 * Represents the possible outcomes of processing [StatsIntent] actions.
 * Currently, this sealed class doesn't contain any results, serving as a placeholder
 * for future expansion.
 */
sealed class StatsResult : MVIResult {
    data object Loading : StatsResult()
    data class Success(
        val stats: SmokeStats
    ) : StatsResult()

    data class Error(val error: Throwable) : StatsResult()
}