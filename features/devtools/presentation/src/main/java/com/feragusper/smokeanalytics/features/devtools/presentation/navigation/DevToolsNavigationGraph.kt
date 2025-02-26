package com.feragusper.smokeanalytics.features.devtools.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.devtools.presentation.DevToolsView
import com.feragusper.smokeanalytics.features.devtools.presentation.DevToolsViewModel
import com.feragusper.smokeanalytics.features.devtools.presentation.navigation.DevToolsNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.devtools.presentation.navigation.DevToolsNavigator.Companion.START

/**
 * Builds the navigation graph for the DevTools feature.
 *
 * This function sets up the navigation route and destination for the developer tools flow,
 * starting with the DevTools screen.
 *
 * @param navigator The navigator used to handle navigation actions for DevTools.
 */
fun NavGraphBuilder.devToolsNavigationGraph(
    navigator: DevToolsNavigator
) {
    // Define the navigation graph for DevTools with a start destination.
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            // Use Hilt to obtain the ViewModel instance.
            val viewModel = hiltViewModel<DevToolsViewModel>()

            // Set the navigator for the ViewModel.
            viewModel.navigator = navigator

            // Display the DevToolsView using the ViewModel.
            DevToolsView(viewModel)
        }
    }
}
