package com.feragusper.smokeanalytics.features.authentication.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

/**
 * Defines intents related to the authentication feature, representing actions the user can initiate.
 */
sealed class AuthenticationIntent : MVIIntent {

    /**
     * Represents an intent to fetch the current user's information.
     */
    data object FetchUser : AuthenticationIntent()

    /**
     * Indicates a request to navigate up in the navigation stack.
     */
    data object NavigateUp : AuthenticationIntent()
}
