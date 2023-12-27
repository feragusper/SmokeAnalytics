package com.feragusper.smokeanalytics.features.settings.navigation

import androidx.navigation.NavController
import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

class SettingsNavigator(navController: NavController) : MVINavigator {
    val navigateToSettings: () -> Unit = { navController.navigate(ROUTE) }

    companion object {
        const val ROUTE = "settings_graph"
        const val START = "settings"
    }

}