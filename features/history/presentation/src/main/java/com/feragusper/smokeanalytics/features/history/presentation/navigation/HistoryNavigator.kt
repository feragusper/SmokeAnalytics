package com.feragusper.smokeanalytics.features.history.presentation.navigation

import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

class HistoryNavigator(
    val navigateToAuthentication: () -> Unit,
    val navigateUp: () -> Unit
) : MVINavigator {

    companion object {
        const val ROUTE = "history_graph"
        const val START = "history"
    }

}