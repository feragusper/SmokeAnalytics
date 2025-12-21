package com.feragusper.smokeanalytics.features.stats.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats

/**
 * Represents the possible outcomes of processing [StatsIntent] actions.
 *
 * This sealed class defines all possible states the Stats module can be in,
 * based on the result of processing a user intent.
 */
sealed class StatsResult : MVIResult {

    /**
     * Indicates that the statistics data is being loaded.
     *
     * This result is used to show a loading indicator while fetching the data.
     */
    data object Loading : StatsResult()

    /**
     * Indicates a successful fetch of statistics data.
     *
     * This result is used to update the UI with the fetched statistics.
     *
     * @property stats The statistics data to be displayed.
     */
    data class Success(
        val stats: SmokeStats
    ) : StatsResult()

    /**
     * Indicates that an error occurred while fetching the statistics data.
     *
     * This result is used to display an error message or a fallback state.
     *
     * @property error The exception that was thrown during the data fetch.
     */
    data class Error(val error: Throwable) : StatsResult()
}
