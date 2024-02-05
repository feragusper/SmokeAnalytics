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
import com.feragusper.smokeanalytics.libraries.design.theme.SmokeAnalyticsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DevToolsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmokeAnalyticsTheme(
                dynamicColor = false,
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Scaffold { innerPadding ->
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