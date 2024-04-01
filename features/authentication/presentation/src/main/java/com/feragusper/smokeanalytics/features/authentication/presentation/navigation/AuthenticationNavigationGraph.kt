package com.feragusper.smokeanalytics.features.authentication.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.authentication.presentation.navigation.AuthenticationNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.authentication.presentation.navigation.AuthenticationNavigator.Companion.START
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationView
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationViewModel

fun NavGraphBuilder.authenticationNavigationGraph(
    authenticationNavigator: AuthenticationNavigator
) {
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            val viewModel = hiltViewModel<AuthenticationViewModel>()
            viewModel.navigator = authenticationNavigator
            AuthenticationView(viewModel)
        }
    }
}
