package com.feragusper.smokeanalytics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator
import com.feragusper.smokeanalytics.features.home.presentation.navigation.homeNavigationGraph
import com.feragusper.smokeanalytics.features.profile.navigation.ProfileNavigator
import com.feragusper.smokeanalytics.features.profile.navigation.profileNavigationGraph
import com.feragusper.smokeanalytics.libraries.design.theme.SmokeAnalyticsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmokeAnalyticsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContainerScreen()
                }
            }
        }
    }
}

sealed class BottomNavigationScreens(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector
) {
    object Home : BottomNavigationScreens(
        HomeNavigator.ROUTE,
        R.string.bottom_navigation_item_home_title,
        Icons.Filled.Home
    )

    object Profile : BottomNavigationScreens(
        ProfileNavigator.ROUTE,
        R.string.bottom_navigation_item_profile_title,
        Icons.Filled.AccountCircle
    )
}

@Composable
private fun MainContainerScreen() {
    val navController = rememberNavController()

    val bottomNavigationItems = listOf(
        BottomNavigationScreens.Home,
        BottomNavigationScreens.Profile,
    )
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        bottomBar = { BottomNavigation(navController, bottomNavigationItems) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        MainScreenNavigationConfigurations(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
        )
    }
}

@Composable
private fun BottomNavigation(
    navController: NavHostController,
    items: List<BottomNavigationScreens>
) {
    BottomNavigation(
        elevation = 0.dp,
    ) {
        val currentRoute = currentRoute(navController)
        items.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = ""
                    )
                },
                label = { Text(stringResource(id = screen.resourceId)) },
                selected = currentRoute == screen.route,
                alwaysShowLabel = false, // This hides the title for the unselected items
                onClick = {
                    // This if check gives us a "singleTop" behavior where we do not create a
                    // second instance of the composable if we are already on that destination
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route)
                    }
                }
            )
        }
    }
}

@Composable
private fun MainScreenNavigationConfigurations(
    navController: NavHostController,
    modifier: Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomNavigationScreens.Home.route
    ) {
        val profileNavigator = ProfileNavigator(navController)
        homeNavigationGraph(HomeNavigator(profileNavigator.navigateToProfile))
        profileNavigationGraph(profileNavigator)
    }
}

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    return navBackStackEntry?.destination?.parent?.route
}

@Preview(showBackground = true)
@Composable
fun MainContainerScreenPreview() {
    SmokeAnalyticsTheme {
        MainContainerScreen()
    }
}