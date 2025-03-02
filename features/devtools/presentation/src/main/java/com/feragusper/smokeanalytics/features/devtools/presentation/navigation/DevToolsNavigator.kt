package com.feragusper.smokeanalytics.features.devtools.presentation.navigation

import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

/**
 * Navigator for the DevTools feature.
 *
 * This navigator handles navigation actions for the developer tools flow,
 * such as navigating to specific tools or returning to the previous screen.
 */
class DevToolsNavigator : MVINavigator {

    companion object {
        // Navigation route for the DevTools graph.
        const val ROUTE = "devtools_graph"

        // Start destination within the DevTools graph.
        const val START = "devtools"
    }
}
