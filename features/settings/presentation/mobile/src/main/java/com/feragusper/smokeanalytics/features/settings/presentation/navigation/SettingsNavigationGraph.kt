package com.feragusper.smokeanalytics.features.settings.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.settings.presentation.SettingsView
import com.feragusper.smokeanalytics.features.settings.presentation.SettingsViewModel
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator.Companion.START

/**
 * Builds the navigation graph for the Settings feature.
 *
 * This function sets up the navigation route and destination for the Settings flow,
 * starting with the Settings screen.
 *
 * @param navigator The navigator used to handle navigation actions for Settings.
 */
fun NavGraphBuilder.settingsNavigationGraph(
    navigator: SettingsNavigator
) {
    // Define the navigation graph for Settings with a start destination.
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            // Use Hilt to obtain the ViewModel instance.
            val viewModel = hiltViewModel<SettingsViewModel>()

            // Set the navigator for the ViewModel.
            viewModel.navigator = navigator

            // Display the SettingsView using the ViewModel.
            SettingsView(viewModel)
        }
    }
}
