package com.feragusper.smokeanalytics.features.home.presentation.web

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke

/**
 * Represents the state of the Home screen.
 *
 * @property displayLoading Whether the loading indicator should be displayed.
 * @property displayRefreshLoading Whether the refresh loading indicator should be displayed.
 * @property smokesPerDay The number of smokes smoked today.
 * @property smokesPerWeek The number of smokes smoked this week.
 * @property smokesPerMonth The number of smokes smoked this month.
 * @property timeSinceLastCigarette The time since the last cigarette.
 * @property latestSmokes The latest smokes smoked.
 * @property error The error that occurred during the last operation.
 */
data class HomeViewState(
    val displayLoading: Boolean = false,
    val displayRefreshLoading: Boolean = false,
    val smokesPerDay: Int? = null,
    val smokesPerWeek: Int? = null,
    val smokesPerMonth: Int? = null,
    val timeSinceLastCigarette: Pair<Long, Long>? = null,
    val latestSmokes: List<Smoke>? = null,
    val error: HomeError? = null,
) {

    /**
     * Represents the errors that can occur in the Home screen.
     */
    sealed interface HomeError {

        /**
         * Represents a generic error.
         */
        data object Generic : HomeError

        /**
         * Represents an error when the user is not logged in.
         */
        data object NotLoggedIn : HomeError
    }
}