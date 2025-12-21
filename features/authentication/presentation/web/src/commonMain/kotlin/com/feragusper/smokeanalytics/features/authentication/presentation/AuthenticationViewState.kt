package com.feragusper.smokeanalytics.features.authentication.presentation

/**
 * Represents the current state of the authentication feature.
 *
 * @property displayLoading Indicates whether the loading indicator should be displayed.
 * @property isLoggedIn Indicates whether the user is currently logged in.
 * @property error The error that occurred during the authentication process, if any.
 */
data class AuthenticationViewState(
    val displayLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: AuthenticationError? = null,
) {
    /**
     * Represents the different types of errors that can occur during the authentication process.
     */
    sealed interface AuthenticationError {

        /**
         * Represents an error that occurred due to a generic error.
         */
        data object Generic : AuthenticationError
    }
}