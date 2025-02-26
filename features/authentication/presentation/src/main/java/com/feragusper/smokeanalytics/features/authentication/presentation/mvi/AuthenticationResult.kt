package com.feragusper.smokeanalytics.features.authentication.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

/**
 * Represents the results of processing [AuthenticationIntent], which will modify the view state.
 */
sealed interface AuthenticationResult : MVIResult {

    /**
     * Indicates that a loading state is in progress.
     */
    object Loading : AuthenticationResult

    /**
     * Indicates that the user has successfully logged in.
     */
    object UserLoggedIn : AuthenticationResult

    /**
     * Represents errors that might occur during the processing of authentication intents.
     */
    sealed interface Error : AuthenticationResult {
        /**
         * A generic error result.
         */
        object Generic : Error
    }

    /**
     * Triggers navigation to the previous screen.
     */
    object NavigateUp : AuthenticationResult
}
