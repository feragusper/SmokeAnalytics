package com.feragusper.smokeanalytics.features.authentication.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

/**
 * Represents the results of processing [AuthenticationIntent], which will modify the view state.
 */
sealed interface AuthenticationResult : MVIResult {
    object Loading : AuthenticationResult

    object UserLoggedIn : AuthenticationResult

    /**
     * Represents errors that might occur during the processing of history intents.
     */
    sealed interface Error : AuthenticationResult {
        object Generic :
            Error
    }

    object NavigateUp : AuthenticationResult
}
