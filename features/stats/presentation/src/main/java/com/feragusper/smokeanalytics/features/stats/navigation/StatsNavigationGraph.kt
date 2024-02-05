package com.feragusper.smokeanalytics.features.stats.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.stats.navigation.StatsNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.stats.navigation.StatsNavigator.Companion.START
import com.feragusper.smokeanalytics.features.stats.presentation.StatsView
import com.feragusper.smokeanalytics.features.stats.presentation.StatsViewModel

fun NavGraphBuilder.statsNavigationGraph(
    navigator: StatsNavigator
) {
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            val viewModel = hiltViewModel<StatsViewModel>()
            viewModel.navigator = navigator
            StatsView(viewModel)
        }
    }
}
