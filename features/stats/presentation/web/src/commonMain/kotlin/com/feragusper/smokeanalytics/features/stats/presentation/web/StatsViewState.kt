package com.feragusper.smokeanalytics.features.stats.presentation.web

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats

/**
 * Represents the state of the Stats screen.
 *
 * @property displayLoading Whether the loading indicator should be displayed.
 * @property stats The stats.
 * @property error The error.
 */
data class StatsViewState(
    val displayLoading: Boolean = false,
    val stats: SmokeStats? = null,
    val error: StatsError? = null,
) {

    /**
     * Represents the errors that can occur in the Stats screen.
     */
    sealed interface StatsError {

        /**
         * Represents the generic error.
         */
        data object Generic : StatsError
    }
}