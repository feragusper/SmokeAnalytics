package com.feragusper.smokeanalytics.features.authentication.presentation.mvi

/**
 * Represents the different results that can be produced by the authentication feature.
 */
sealed interface AuthenticationResult {

    /**
     * Represents the result of a successful authentication attempt.
     */
    data object Loading : AuthenticationResult

    /**
     * Represents the result of a successful authentication attempt.
     */
    data object UserLoggedIn : AuthenticationResult

    /**
     * Represents the result of a successful authentication attempt.
     */
    data object UserLoggedOut : AuthenticationResult

    /**
     * Represents the result of a successful authentication attempt.
     */
    data object NavigateUp : AuthenticationResult

    /**
     * Represents the result of a failed authentication attempt.
     */
    sealed interface Error : AuthenticationResult {
        /**
         * Represents the result of a failed authentication attempt due to a generic error.
         */
        data object Generic : Error
    }
}