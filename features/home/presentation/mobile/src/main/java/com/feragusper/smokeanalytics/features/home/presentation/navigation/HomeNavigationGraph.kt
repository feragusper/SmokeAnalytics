package com.feragusper.smokeanalytics.features.home.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.home.presentation.HomeView
import com.feragusper.smokeanalytics.features.home.presentation.HomeViewModel
import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator.Companion.START

/**
 * Builds the navigation graph for the Home feature.
 *
 * This function sets up the navigation route and destination for the home flow,
 * starting with the Home screen.
 *
 * @param homeNavigator The navigator used to handle navigation actions for Home.
 */
fun NavGraphBuilder.homeNavigationGraph(
    homeNavigator: HomeNavigator,
    onFabConfigChanged: (Boolean, (() -> Unit)?) -> Unit,
) {
    // Define the navigation graph for Home with a start destination.
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            // Use Hilt to obtain the ViewModel instance.
            val viewModel = hiltViewModel<HomeViewModel>()

            // Set the navigator for the ViewModel.
            viewModel.navigator = homeNavigator

            // Display the HomeView using the ViewModel.
            HomeView(
                viewModel,
                onFabConfigChanged,
            )
        }
    }
}
