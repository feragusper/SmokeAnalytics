package com.feragusper.smokeanalytics.features.authentication.presentation.navigation

import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

class AuthenticationNavigator(
    val navigateUp: () -> Unit
) : MVINavigator {

    companion object {
        const val ROUTE = "authentication_graph"
        const val START = "authentication"
    }

}