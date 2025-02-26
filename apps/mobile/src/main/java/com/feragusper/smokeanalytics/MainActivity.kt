package com.feragusper.smokeanalytics

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationActivity
import com.feragusper.smokeanalytics.features.history.presentation.HistoryActivity
import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator
import com.feragusper.smokeanalytics.features.home.presentation.navigation.homeNavigationGraph
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.settingsNavigationGraph
import com.feragusper.smokeanalytics.features.stats.presentation.navigation.StatsNavigator
import com.feragusper.smokeanalytics.features.stats.presentation.navigation.statsNavigationGraph
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main activity that serves as the entry point of the application.
 * It sets up the application's theme and initializes the main container screen.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is starting. This is where most initialization should go:
     * setting up Jetpack Compose content and configuring the theme with [SmokeAnalyticsTheme].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmokeAnalyticsTheme(
                dynamicColor = false,
            ) {
                // A surface container using the 'background' color from the theme.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContainerScreen(
                        navigateToAuthentication = {
                            startActivity(Intent(this, AuthenticationActivity::class.java))
                        },
                        navigateToHistory = {
                            startActivity(Intent(this, HistoryActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

/**
 * Composable function that sets up the main screen with bottom navigation and a snackbar host.
 *
 * @param navigateToAuthentication Lambda function to navigate to the AuthenticationActivity.
 * @param navigateToHistory Lambda function to navigate to the HistoryActivity.
 */
@Composable
private fun MainContainerScreen(
    navigateToAuthentication: () -> Unit,
    navigateToHistory: () -> Unit
) {
    val navController = rememberNavController()

    val bottomNavigationItems = listOf(
        BottomNavigationScreens.Home,
        BottomNavigationScreens.Stats,
        BottomNavigationScreens.Settings,
    )
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        bottomBar = { BottomNavigation(navController, bottomNavigationItems) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        MainScreenNavigationConfigurations(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            navigateToAuthentication = navigateToAuthentication,
            navigateToHistory = navigateToHistory
        )
    }
}

/**
 * Defines the UI and behavior of the bottom navigation bar.
 *
 * @param navController The [NavHostController] used for navigation.
 * @param items The list of screens to show in the bottom navigation.
 */
@Composable
private fun BottomNavigation(
    navController: NavHostController,
    items: List<BottomNavigationScreens>
) {
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(screen.iconId),
                        contentDescription = ""
                    )
                },
                label = { Text(stringResource(id = screen.labelId)) },
                selected = currentRoute(navController) == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to avoid building a large back stack.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when re-selecting the same item.
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item.
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Sets up the navigation graph for the main screen, defining navigation paths to different features.
 *
 * @param navController The [NavHostController] used for navigation.
 * @param modifier A [Modifier] applied to the [NavHost] composable.
 * @param navigateToAuthentication Lambda function to navigate to the AuthenticationActivity.
 * @param navigateToHistory Lambda function to navigate to the HistoryActivity.
 */
@Composable
private fun MainScreenNavigationConfigurations(
    navController: NavHostController,
    modifier: Modifier,
    navigateToAuthentication: () -> Unit,
    navigateToHistory: () -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomNavigationScreens.Home.route
    ) {
        val settingsNavigator = SettingsNavigator(navController)
        homeNavigationGraph(
            HomeNavigator(
                navigateToAuthentication = navigateToAuthentication,
                navigateToSettings = settingsNavigator.navigateToSettings,
                navigateToHistory = navigateToHistory
            )
        )
        statsNavigationGraph(StatsNavigator())
        settingsNavigationGraph(settingsNavigator)
    }
}

/**
 * Determines the current route based on the navigation controller's state.
 *
 * @param navController The [NavHostController] used for navigation.
 * @return The current route as a [String], or `null` if there's no current route.
 */
@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.parent?.route
}

/**
 * Represents the screens available in the bottom navigation bar of the app.
 * Each screen is defined by a route, a label resource ID, and an icon resource ID.
 */
private sealed class BottomNavigationScreens(
    val route: String,
    @StringRes val labelId: Int,
    @DrawableRes val iconId: Int
) {

    /**
     * The Home screen, representing the main view of the app.
     */
    object Home : BottomNavigationScreens(
        HomeNavigator.ROUTE,
        R.string.bottom_navigation_item_home_title,
        R.drawable.ic_home
    )

    /**
     * The Stats screen, showing statistics and analytics related to user activity.
     */
    object Stats : BottomNavigationScreens(
        StatsNavigator.ROUTE,
        R.string.bottom_navigation_item_stats_title,
        R.drawable.ic_stats
    )

    /**
     * The Settings screen, allowing users to configure app settings and preferences.
     */
    object Settings : BottomNavigationScreens(
        SettingsNavigator.ROUTE,
        R.string.bottom_navigation_item_settings_title,
        R.drawable.ic_settings
    )
}

@Preview(showBackground = true)
@Composable
private fun MainContainerScreenPreview() {
    SmokeAnalyticsTheme {
        MainContainerScreen(
            navigateToAuthentication = {},
            navigateToHistory = {}
        )
    }
}
