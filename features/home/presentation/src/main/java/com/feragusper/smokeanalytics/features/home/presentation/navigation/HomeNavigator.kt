package com.feragusper.smokeanalytics.features.home.presentation.navigation

import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

class HomeNavigator(
    val navigateToAuthentication: () -> Unit,
    val navigateToSettings: () -> Unit,
    val navigateToHistory: () -> Unit
) : MVINavigator {

    companion object {
        const val ROUTE = "home_graph"
        const val START = "home"
    }

}