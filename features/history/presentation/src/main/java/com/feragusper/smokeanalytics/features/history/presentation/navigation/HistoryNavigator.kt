package com.feragusper.smokeanalytics.features.history.presentation.navigation

import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

/**
 * Navigator for the History feature.
 *
 * This navigator handles navigation actions for the history flow,
 * such as navigating to the authentication screen or navigating back.
 *
 * @property navigateToAuthentication Lambda function to navigate to the Authentication screen.
 * @property navigateUp Lambda function to navigate back to the previous screen.
 */
class HistoryNavigator(
    val navigateToAuthentication: () -> Unit,
    val navigateUp: () -> Unit
) : MVINavigator {

    companion object {
        // Navigation route for the History graph.
        const val ROUTE = "history_graph"

        // Start destination within the History graph.
        const val START = "history"
    }
}
