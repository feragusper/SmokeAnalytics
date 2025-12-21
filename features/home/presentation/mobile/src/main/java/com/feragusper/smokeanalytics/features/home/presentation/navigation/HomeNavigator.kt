package com.feragusper.smokeanalytics.features.home.presentation.navigation

import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

/**
 * Navigator for the Home feature.
 *
 * This navigator handles navigation actions for the home flow,
 * such as navigating to the authentication screen, settings screen, or history screen.
 *
 * @property navigateToAuthentication Lambda function to navigate to the Authentication screen.
 * @property navigateToSettings Lambda function to navigate to the Settings screen.
 * @property navigateToHistory Lambda function to navigate to the History screen.
 */
class HomeNavigator(
    val navigateToAuthentication: () -> Unit,
    val navigateToSettings: () -> Unit,
    val navigateToHistory: () -> Unit
) : MVINavigator {

    companion object {
        // Navigation route for the Home graph.
        const val ROUTE = "home_graph"

        // Start destination within the Home graph.
        const val START = "home"
    }
}
