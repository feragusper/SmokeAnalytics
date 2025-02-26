package com.feragusper.smokeanalytics.features.stats.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.stats.presentation.StatsView
import com.feragusper.smokeanalytics.features.stats.presentation.StatsViewModel
import com.feragusper.smokeanalytics.features.stats.presentation.navigation.StatsNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.stats.presentation.navigation.StatsNavigator.Companion.START

/**
 * Builds the navigation graph for the Stats feature.
 *
 * This function sets up the navigation route and destination for the Stats flow,
 * starting with the Stats screen.
 *
 * @param navigator The navigator used to handle navigation actions for Stats.
 */
fun NavGraphBuilder.statsNavigationGraph(
    navigator: StatsNavigator
) {
    // Define the navigation graph for Stats with a start destination.
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            // Use Hilt to obtain the ViewModel instance.
            val viewModel = hiltViewModel<StatsViewModel>()

            // Set the navigator for the ViewModel.
            viewModel.navigator = navigator

            // Display the StatsView using the ViewModel.
            StatsView(viewModel)
        }
    }
}
