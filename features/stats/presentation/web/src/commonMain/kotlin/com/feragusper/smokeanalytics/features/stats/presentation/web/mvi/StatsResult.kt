package com.feragusper.smokeanalytics.features.stats.presentation.web.mvi

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats

/**
 * Represents the results that can be returned from the Stats screen.
 */
sealed interface StatsResult {

    /**
     * Represents the result of a loading operation.
     */
    data object Loading : StatsResult

    /**
     * Represents the result of a successful fetch of the stats.
     *
     * @property stats The stats.
     */
    data class Success(val stats: SmokeStats) : StatsResult

    /**
     * Represents the result of a generic error.
     *
     * @property error The error.
     */
    data class Error(val error: Throwable) : StatsResult
}