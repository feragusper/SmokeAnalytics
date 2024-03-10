package com.feragusper.smokeanalytics.features.history.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.history.presentation.navigation.HistoryNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.history.presentation.navigation.HistoryNavigator.Companion.START
import com.feragusper.smokeanalytics.features.history.presentation.HistoryView
import com.feragusper.smokeanalytics.features.history.presentation.HistoryViewModel

fun NavGraphBuilder.historyNavigationGraph(
    historyNavigator: HistoryNavigator
) {
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            val viewModel = hiltViewModel<HistoryViewModel>()
            viewModel.navigator = historyNavigator
            HistoryView(viewModel)
        }
    }
}
