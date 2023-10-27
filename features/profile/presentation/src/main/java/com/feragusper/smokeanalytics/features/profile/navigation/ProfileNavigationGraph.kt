package com.feragusper.smokeanalytics.features.profile.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.profile.navigation.ProfileNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.profile.navigation.ProfileNavigator.Companion.START
import com.feragusper.smokeanalytics.features.profile.presentation.ProfileView
import com.feragusper.smokeanalytics.features.profile.presentation.ProfileViewModel

fun NavGraphBuilder.profileNavigationGraph(
    navigator: ProfileNavigator
) {
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            val viewModel = hiltViewModel<ProfileViewModel>()
            viewModel.navigator = navigator
            ProfileView(viewModel)
        }
    }
}
