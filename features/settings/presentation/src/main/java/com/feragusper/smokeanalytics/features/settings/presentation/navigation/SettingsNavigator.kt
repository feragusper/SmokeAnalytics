package com.feragusper.smokeanalytics.features.settings.presentation.navigation

import androidx.navigation.NavController
import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator

/**
 * Navigator for the Settings feature.
 *
 * This navigator handles navigation actions for the settings flow,
 * such as navigating to the Settings screen.
 *
 * @property navigateToSettings Lambda function to navigate to the Settings screen.
 */
class SettingsNavigator(
    private val navController: NavController
) : MVINavigator {

    /**
     * Navigates to the Settings screen.
     *
     * This lambda is used to trigger the navigation action, ensuring consistency with the MVI pattern.
     */
    val navigateToSettings: () -> Unit = { navController.navigate(ROUTE) }

    companion object {
        // Navigation route for the Settings graph.
        const val ROUTE = "settings_graph"

        // Start destination within the Settings graph.
        const val START = "settings"
    }
}
