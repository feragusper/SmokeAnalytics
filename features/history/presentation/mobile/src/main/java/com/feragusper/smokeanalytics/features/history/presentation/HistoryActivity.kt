package com.feragusper.smokeanalytics.features.history.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationActivity
import com.feragusper.smokeanalytics.features.history.presentation.navigation.HistoryNavigator
import com.feragusper.smokeanalytics.features.history.presentation.navigation.historyNavigationGraph
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for the History feature.
 * This activity hosts the navigation graph for viewing smoke history.
 */
@AndroidEntryPoint
class HistoryActivity : ComponentActivity() {

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

                    // Host the history navigation graph.
                    NavHost(
                        navController = navController,
                        startDestination = HistoryNavigator.ROUTE
                    ) {
                        historyNavigationGraph(
                            HistoryNavigator(
                                navigateToAuthentication = {
                                    startActivity(
                                        Intent(
                                            this@HistoryActivity,
                                            AuthenticationActivity::class.java
                                        )
                                    )
                                },
                                navigateUp = { finish() }
                            )
                        )
                    }
                }
            }
        }
    }
}
