package com.feragusper.smokeanalytics.features.profile.navigation

import androidx.navigation.NavController
import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

class ProfileNavigator(navController: NavController) : MVINavigator {
    val navigateToProfile: () -> Unit = { navController.navigate(ROUTE) }

    companion object {
        const val ROUTE = "profile_graph"
        const val START = "profile"
    }

}