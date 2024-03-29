package com.feragusper.smokeanalytics.features.home.presentation.mvi

import com.feragusper.smokeanalytics.features.home.domain.SmokeCountListResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

/**
 * Represents the possible outcomes of processing [HomeIntent] actions.
 */
sealed interface HomeResult : MVIResult {

    /**
     * Indicates that the application is currently loading or processing data.
     */
    object Loading : HomeResult
    object NotLoggedIn : HomeResult
    object GoToLogin : HomeResult
    object GoToHistory : HomeResult
    object AddSmokeSuccess : HomeResult
    object EditSmokeSuccess : HomeResult
    object DeleteSmokeSuccess : HomeResult
    sealed interface Error : HomeResult {
        object Generic :
            Error

        object NotLoggedIn :
            Error
    }

    /**
     * Indicates a successful fetch of smoke data, containing smoke counts and latest smokes.
     *
     * @property smokeCountListResult The result containing counts of smokes and latest smokes.
     */
    data class FetchSmokesSuccess(val smokeCountListResult: SmokeCountListResult) : HomeResult
    object FetchSmokesError : HomeResult
    data class UpdateTimeSinceLastCigarette(val timeSinceLastCigarette: Pair<Long, Long>) :
        HomeResult
}
