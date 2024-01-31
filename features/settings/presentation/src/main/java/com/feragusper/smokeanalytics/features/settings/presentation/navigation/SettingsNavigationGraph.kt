package com.feragusper.smokeanalytics.features.settings.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator.Companion.START
import com.feragusper.smokeanalytics.features.settings.presentation.SettingsView
import com.feragusper.smokeanalytics.features.settings.presentation.SettingsViewModel

fun NavGraphBuilder.settingsNavigationGraph(
    navigator: SettingsNavigator
) {
    navigation(startDestination = START, route = ROUTE) {
        navOptions { launchSingleTop = true }
        composable(route = START) {
            val viewModel = hiltViewModel<SettingsViewModel>()
            viewModel.navigator = navigator
            SettingsView(viewModel)
        }
    }
}
