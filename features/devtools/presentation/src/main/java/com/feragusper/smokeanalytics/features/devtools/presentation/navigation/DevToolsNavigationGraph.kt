package com.feragusper.smokeanalytics.features.devtools.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.devtools.presentation.DevToolsView
import com.feragusper.smokeanalytics.features.devtools.presentation.DevToolsViewModel
import com.feragusper.smokeanalytics.features.devtools.presentation.navigation.DevToolsNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.devtools.presentation.navigation.DevToolsNavigator.Companion.START

fun NavGraphBuilder.devToolsNavigationGraph(
    navigator: DevToolsNavigator
) {
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            val viewModel = hiltViewModel<DevToolsViewModel>()
            viewModel.navigator = navigator
            DevToolsView(viewModel)
        }
    }
}