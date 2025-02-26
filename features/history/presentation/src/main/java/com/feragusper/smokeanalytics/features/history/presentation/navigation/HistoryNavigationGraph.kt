package com.feragusper.smokeanalytics.features.history.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.history.presentation.HistoryView
import com.feragusper.smokeanalytics.features.history.presentation.HistoryViewModel
import com.feragusper.smokeanalytics.features.history.presentation.navigation.HistoryNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.history.presentation.navigation.HistoryNavigator.Companion.START

/**
 * Builds the navigation graph for the History feature.
 *
 * This function sets up the navigation route and destination for the history flow,
 * starting with the History screen.
 *
 * @param historyNavigator The navigator used to handle navigation actions for History.
 */
fun NavGraphBuilder.historyNavigationGraph(
    historyNavigator: HistoryNavigator
) {
    // Define the navigation graph for History with a start destination.
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            // Use Hilt to obtain the ViewModel instance.
            val viewModel = hiltViewModel<HistoryViewModel>()

            // Set the navigator for the ViewModel.
            viewModel.navigator = historyNavigator

            // Display the HistoryView using the ViewModel.
            HistoryView(viewModel)
        }
    }
}
