package com.feragusper.smokeanalytics.features.chatbot.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feragusper.smokeanalytics.features.chatbot.presentation.ChatbotView
import com.feragusper.smokeanalytics.features.chatbot.presentation.ChatbotViewModel
import com.feragusper.smokeanalytics.features.chatbot.presentation.navigation.ChatbotNavigator.Companion.ROUTE
import com.feragusper.smokeanalytics.features.chatbot.presentation.navigation.ChatbotNavigator.Companion.START

/**
 * Builds the navigation graph for the Stats feature.
 *
 * This function sets up the navigation route and destination for the Stats flow,
 * starting with the Stats screen.
 *
 * @param navigator The navigator used to handle navigation actions for Stats.
 */
fun NavGraphBuilder.chatbotNavigationGraph(
    navigator: ChatbotNavigator
) {
    // Define the navigation graph for Stats with a start destination.
    navigation(startDestination = START, route = ROUTE) {
        composable(route = START) {
            // Use Hilt to obtain the ViewModel instance.
            val viewModel = hiltViewModel<ChatbotViewModel>()

            // Set the navigator for the ViewModel.
            viewModel.navigator = navigator

            // Display the StatsView using the ViewModel.
            ChatbotView(viewModel)
        }
    }
}
