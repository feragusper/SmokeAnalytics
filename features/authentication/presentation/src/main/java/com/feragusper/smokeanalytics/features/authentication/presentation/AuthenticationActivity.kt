package com.feragusper.smokeanalytics.features.authentication.presentation

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
import com.feragusper.smokeanalytics.features.authentication.presentation.navigation.AuthenticationNavigator
import com.feragusper.smokeanalytics.features.authentication.presentation.navigation.authenticationNavigationGraph
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : ComponentActivity() {
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
                    Scaffold { innerPadding ->

                        val navController = rememberNavController()

                        NavHost(
                            modifier = Modifier.padding(innerPadding),
                            navController = navController,
                            startDestination = AuthenticationNavigator.ROUTE
                        ) {
                            authenticationNavigationGraph(AuthenticationNavigator { finish() })
                        }
                    }
                }
            }
        }
    }
}