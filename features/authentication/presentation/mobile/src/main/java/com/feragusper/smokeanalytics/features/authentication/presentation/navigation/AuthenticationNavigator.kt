package com.feragusper.smokeanalytics.features.authentication.presentation.navigation

import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

/**
 * Navigator for the Authentication feature.
 *
 * This navigator handles navigation actions for the authentication flow,
 * such as navigating back to the previous screen.
 *
 * @property navigateUp Lambda function to navigate back to the previous screen.
 */
class AuthenticationNavigator(
    val navigateUp: () -> Unit
) : MVINavigator {

    companion object {
        // Navigation route for the authentication graph.
        const val ROUTE = "authentication_graph"

        // Start destination within the authentication graph.
        const val START = "authentication"
    }
}
