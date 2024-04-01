package com.feragusper.smokeanalytics.features.authentication.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

/**
 * Defines intents related to the history feature, representing actions the user can initiate.
 */
sealed class AuthenticationIntent : MVIIntent {

    /**
     * Represents an intent to fetch the current user's information.
     */
    object FetchUser : AuthenticationIntent()

    /**
     * Indicates a request to navigate up in the navigation stack.
     */
    object NavigateUp : AuthenticationIntent()
}
