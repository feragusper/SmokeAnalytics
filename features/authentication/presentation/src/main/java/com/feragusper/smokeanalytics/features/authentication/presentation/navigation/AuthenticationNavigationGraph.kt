package com.feragusper.smokeanalytics.features.authentication.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationView
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationViewModel
import com.feragusper.smokeanalytics.features.authentication.presentation.navigation.AuthenticationNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.authentication.presentation.navigation.AuthenticationNavigator.Companion.START

/**
 * Builds the navigation graph for the authentication feature.
 *
 * This function sets up the navigation route and destination for the authentication flow,
 * starting with the authentication screen.
 *
 * @param authenticationNavigator The navigator used to handle navigation actions for authentication.
 */
fun NavGraphBuilder.authenticationNavigationGraph(
    authenticationNavigator: AuthenticationNavigator
) {
    // Define the navigation graph for authentication with a start destination.
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            // Use Hilt to obtain the ViewModel instance.
            val viewModel = hiltViewModel<AuthenticationViewModel>()

            // Set the navigator for the ViewModel.
            viewModel.navigator = authenticationNavigator

            // Display the AuthenticationView using the ViewModel.
            AuthenticationView(viewModel)
        }
    }
}
