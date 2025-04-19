package com.feragusper.smokeanalytics

import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.exyte.animatednavbar.animation.balltrajectory.Parabolic
import com.exyte.animatednavbar.animation.indendshape.Height
import com.exyte.animatednavbar.animation.indendshape.shapeCornerRadius
import com.exyte.animatednavbar.items.dropletbutton.DropletButton
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationActivity
import com.feragusper.smokeanalytics.features.chatbot.presentation.navigation.ChatbotNavigator
import com.feragusper.smokeanalytics.features.chatbot.presentation.navigation.chatbotNavigationGraph
import com.feragusper.smokeanalytics.features.history.presentation.HistoryActivity
import com.feragusper.smokeanalytics.features.home.presentation.mvi.compose.HomeViewState.TestTags.Companion.BUTTON_ADD_SMOKE
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
        BottomNavigationScreens.Chatbot,
        BottomNavigationScreens.Settings,
    )
    val snackbarHostState = remember { SnackbarHostState() }

    var fabAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showFab by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomNavigation(navController, bottomNavigationItems) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab && currentRoute(navController) == BottomNavigationScreens.Home.route,
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 }),
            ) {
                FloatingActionButton(
                    modifier = Modifier.testTag(BUTTON_ADD_SMOKE),
                    onClick = { fabAction?.invoke() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(com.feragusper.smokeanalytics.features.home.presentation.R.drawable.ic_cigarette),
                            contentDescription = ""
                        )
                        Text(
                            text = stringResource(com.feragusper.smokeanalytics.features.home.presentation.R.string.home_button_track),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        MainScreenNavigationConfigurations(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            navigateToAuthentication = navigateToAuthentication,
            navigateToHistory = navigateToHistory,
            onFabConfigChanged = { isVisible, action ->
                showFab = isVisible
                fabAction = action
            },
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
    var selectedIndex by remember { mutableStateOf(0) }

    AnimatedNavigationBar(
        selectedIndex = selectedIndex,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 40.dp)
            .height(85.dp),
        ballColor = MaterialTheme.colorScheme.primary,
        cornerRadius = shapeCornerRadius(25.dp),
        ballAnimation = Parabolic(tween(500, easing = LinearOutSlowInEasing)),
        indentAnimation = Height(
            indentWidth = 56.dp,
            indentHeight = 15.dp,
            animationSpec = tween(
                1000,
                easing = { OvershootInterpolator().getInterpolation(it) })
        )
    ) {
        items.forEachIndexed { index, screen ->
            DropletButton(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                isSelected = selectedIndex == index,
                icon = screen.iconId,
                iconColor = MaterialTheme.colorScheme.onSurface,
                dropletColor = MaterialTheme.colorScheme.primary,
                animationSpec = tween(durationMillis = 500, easing = LinearEasing),
                size = 24.dp,
                onClick = {
                    selectedIndex = index
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
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
    onFabConfigChanged: (Boolean, (() -> Unit)?) -> Unit,
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
            ),
            onFabConfigChanged = onFabConfigChanged,
        )
        statsNavigationGraph(StatsNavigator())
        chatbotNavigationGraph(ChatbotNavigator())
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
    @DrawableRes val iconId: Int
) {

    /**
     * The Home screen, representing the main view of the app.
     */
    data object Home : BottomNavigationScreens(
        HomeNavigator.ROUTE,
        R.drawable.ic_home
    )

    /**
     * The Stats screen, showing statistics and analytics related to user activity.
     */
    data object Stats : BottomNavigationScreens(
        StatsNavigator.ROUTE,
        R.drawable.ic_stats
    )

    /**
     * The Chatbot screen, allowing users to interact with the AI assistant.
     */
    data object Chatbot : BottomNavigationScreens(
        ChatbotNavigator.ROUTE,
        R.drawable.ic_chatbot
    )

    /**
     * The Settings screen, allowing users to configure app settings and preferences.
     */
    data object Settings : BottomNavigationScreens(
        SettingsNavigator.ROUTE,
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
