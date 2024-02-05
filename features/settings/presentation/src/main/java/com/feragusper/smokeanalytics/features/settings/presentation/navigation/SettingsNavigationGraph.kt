package com.feragusper.smokeanalytics.features.settings.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.settings.presentation.SettingsView
import com.feragusper.smokeanalytics.features.settings.presentation.SettingsViewModel
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator.Companion.START

fun NavGraphBuilder.settingsNavigationGraph(
    navigator: SettingsNavigator
) {
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            val viewModel = hiltViewModel<SettingsViewModel>()
            viewModel.navigator = navigator
            SettingsView(viewModel)
        }
    }
}
