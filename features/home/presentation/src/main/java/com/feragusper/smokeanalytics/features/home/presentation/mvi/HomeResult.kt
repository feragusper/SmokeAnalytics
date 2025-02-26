package com.feragusper.smokeanalytics.features.home.presentation.mvi

import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

/**
 * Represents the possible outcomes of processing [HomeIntent] actions.
 *
 * This sealed interface defines all possible states the Home module can be in,
 * based on the result of processing a user intent.
 */
sealed interface HomeResult : MVIResult {

    /**
     * Indicates that the application is currently loading or processing data.
     * This is typically used to show a loading indicator.
     */
    object Loading : HomeResult

    /**
     * Indicates that a refresh operation is in progress.
     */
    object RefreshLoading : HomeResult

    /**
     * Indicates that the user is not logged in.
     */
    object NotLoggedIn : HomeResult

    /**
     * Triggers navigation to the authentication screen.
     */
    object GoToAuthentication : HomeResult

    /**
     * Triggers navigation to the smoke history screen.
     */
    object GoToHistory : HomeResult

    /**
     * Indicates that a smoke event was successfully added.
     */
    object AddSmokeSuccess : HomeResult

    /**
     * Indicates that a smoke event was successfully edited.
     */
    object EditSmokeSuccess : HomeResult

    /**
     * Indicates that a smoke event was successfully deleted.
     */
    object DeleteSmokeSuccess : HomeResult

    /**
     * Represents errors that might occur during the processing of home intents.
     */
    sealed interface Error : HomeResult {
        /**
         * A generic error result.
         */
        object Generic : Error

        /**
         * Error indicating that the user is not logged in.
         */
        object NotLoggedIn : Error
    }

    /**
     * Indicates a successful fetch of smoke data, containing smoke counts and latest smokes.
     *
     * @property smokeCountListResult The result containing counts of smokes and latest smokes.
     */
    data class FetchSmokesSuccess(
        val smokeCountListResult: SmokeCountListResult
    ) : HomeResult

    /**
     * Indicates an error occurred while fetching smoke data.
     */
    object FetchSmokesError : HomeResult

    /**
     * Updates the time elapsed since the last cigarette.
     *
     * @property timeSinceLastCigarette The time elapsed since the last smoke event,
     * represented as a pair of hours and minutes.
     */
    data class UpdateTimeSinceLastCigarette(
        val timeSinceLastCigarette: Pair<Long, Long>
    ) : HomeResult
}
