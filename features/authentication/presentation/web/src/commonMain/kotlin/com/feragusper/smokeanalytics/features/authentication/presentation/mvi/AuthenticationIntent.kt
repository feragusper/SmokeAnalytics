package com.feragusper.smokeanalytics.features.authentication.presentation.mvi

/**
 * Represents the different intents that can be sent to the authentication feature.
 */
sealed interface AuthenticationIntent {

    /**
     * Represents the intent to fetch the current authentication session.
     */
    data object FetchUser : AuthenticationIntent

    /**
     * Represents the intent to sign in with Google.
     */
    data object SignInWithGoogle : AuthenticationIntent

    /**
     * Represents the intent to sign out the current user.
     */
    data object SignOut : AuthenticationIntent

    /**
     * Represents the intent to navigate back to the previous screen.
     */
    data object NavigateUp : AuthenticationIntent
}