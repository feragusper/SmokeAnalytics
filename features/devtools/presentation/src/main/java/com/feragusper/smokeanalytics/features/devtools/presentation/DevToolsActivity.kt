package com.feragusper.smokeanalytics.features.devtools.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.feragusper.smokeanalytics.features.devtools.presentation.navigation.DevToolsNavigator
import com.feragusper.smokeanalytics.features.devtools.presentation.navigation.devToolsNavigationGraph
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for the Developer Tools feature.
 * This activity hosts the navigation graph for debugging and development utilities.
 */
@AndroidEntryPoint
class DevToolsActivity : ComponentActivity() {

    /**
     * Called when the activity is starting.
     * This is where the UI is set up and the navigation graph is initialized.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply the custom SmokeAnalyticsTheme for consistent theming.
            SmokeAnalyticsTheme(
                dynamicColor = false,
            ) {
                // A surface container using the 'background' color from the theme.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Remember the NavController for managing navigation within this feature.
                    val navController = rememberNavController()

                    Scaffold { innerPadding ->
                        // Host the dev tools navigation graph.
                        NavHost(
                            modifier = Modifier.padding(innerPadding),
                            navController = navController,
                            startDestination = DevToolsNavigator.ROUTE
                        ) {
                            devToolsNavigationGraph(DevToolsNavigator())
                        }
                    }
                }
            }
        }
    }
}
