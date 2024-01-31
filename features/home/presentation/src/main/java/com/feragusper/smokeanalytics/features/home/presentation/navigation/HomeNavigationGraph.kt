package com.feragusper.smokeanalytics.features.home.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator.Companion.START
import com.feragusper.smokeanalytics.features.home.presentation.HomeView
import com.feragusper.smokeanalytics.features.home.presentation.HomeViewModel

fun NavGraphBuilder.homeNavigationGraph(
    homeNavigator: HomeNavigator
) {
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            val viewModel = hiltViewModel<HomeViewModel>()
            viewModel.navigator = homeNavigator
            HomeView(viewModel)
        }
    }
}
