package com.feragusper.smokeanalytics

import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.exyte.animatednavbar.AnimatedNavigationBar
import com.exyte.animatednavbar.animation.balltrajectory.Parabolic
import com.exyte.animatednavbar.animation.indendshape.Height
import com.exyte.animatednavbar.animation.indendshape.shapeCornerRadius
import com.exyte.animatednavbar.items.dropletbutton.DropletButton
import com.feragusper.smokeanalytics.features.authentication.presentation.AuthenticationActivity
import com.feragusper.smokeanalytics.features.home.domain.ElapsedTone
import com.feragusper.smokeanalytics.features.home.presentation.mvi.compose.HomeViewState.TestTags.Companion.BUTTON_ADD_SMOKE
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main activity that serves as the entry point of the application.
 * It sets up the application's theme and initializes the main container screen.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var installStateListener: InstallStateUpdatedListener

    private var availableUpdateInfo: AppUpdateInfo? = null
    private var inAppUpdatePrompt by mutableStateOf<InAppUpdatePrompt?>(null)
    private var restartUpdateReady by mutableStateOf(false)
    private var hasPromptedForUpdateInSession = false
    private var hasPromptedForRestartInSession = false

    private val inAppUpdateLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            refreshInAppUpdateState()
        }

    /**
     * Called when the activity is starting. This is where most initialization should go:
     * setting up Jetpack Compose content and configuring the theme with [SmokeAnalyticsTheme].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        installStateListener = InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    restartUpdateReady = true
                    inAppUpdatePrompt = null
                    hasPromptedForRestartInSession = true
                }

                InstallStatus.INSTALLED,
                InstallStatus.CANCELED,
                InstallStatus.FAILED -> {
                    restartUpdateReady = false
                    availableUpdateInfo = null
                }

                else -> Unit
            }
        }
        appUpdateManager.registerListener(installStateListener)
        refreshInAppUpdateState()
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
                        inAppUpdatePrompt = inAppUpdatePrompt,
                        restartUpdateReady = restartUpdateReady,
                        onDismissUpdatePrompt = { inAppUpdatePrompt = null },
                        onStartUpdate = ::startFlexibleUpdate,
                        onDismissRestartPrompt = { restartUpdateReady = false },
                        onCompleteDownloadedUpdate = ::completeDownloadedUpdate,
                        navigateToAuthentication = {
                            startActivity(Intent(this, AuthenticationActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshInAppUpdateState()
    }

    override fun onDestroy() {
        appUpdateManager.unregisterListener(installStateListener)
        super.onDestroy()
    }

    private fun refreshInAppUpdateState() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                availableUpdateInfo = info
                when {
                    info.installStatus() == InstallStatus.DOWNLOADED -> {
                        restartUpdateReady = true
                    }

                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE)) &&
                        !hasPromptedForUpdateInSession -> {
                        hasPromptedForUpdateInSession = true
                        inAppUpdatePrompt = InAppUpdatePrompt(
                            availableVersionCode = info.availableVersionCode(),
                            stalenessDays = info.clientVersionStalenessDays(),
                            priority = info.updatePriority(),
                        )
                    }

                    info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS &&
                        info.isUpdateTypeAllowed(AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)) -> {
                        startUpdateFlow(info, AppUpdateType.IMMEDIATE)
                    }

                    info.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE &&
                        !hasPromptedForRestartInSession -> {
                        restartUpdateReady = false
                    }
                }
            }
            .addOnFailureListener {
                availableUpdateInfo = null
            }
    }

    private fun startFlexibleUpdate() {
        val info = availableUpdateInfo ?: return
        startUpdateFlow(info, AppUpdateType.FLEXIBLE)
    }

    private fun startUpdateFlow(
        info: AppUpdateInfo,
        updateType: Int,
    ) {
        val started = appUpdateManager.startUpdateFlowForResult(
            info,
            inAppUpdateLauncher,
            AppUpdateOptions.defaultOptions(updateType),
        )
        if (started) {
            inAppUpdatePrompt = null
        }
    }

    private fun completeDownloadedUpdate() {
        appUpdateManager.completeUpdate()
        restartUpdateReady = false
    }
}

/**
 * Composable function that sets up the main screen with bottom navigation and a snackbar host.
 *
 * @param navigateToAuthentication Lambda function to navigate to the AuthenticationActivity.
 */
@Composable
private fun MainContainerScreen(
    inAppUpdatePrompt: InAppUpdatePrompt?,
    restartUpdateReady: Boolean,
    onDismissUpdatePrompt: () -> Unit,
    onStartUpdate: () -> Unit,
    onDismissRestartPrompt: () -> Unit,
    onCompleteDownloadedUpdate: () -> Unit,
    navigateToAuthentication: () -> Unit,
) {
    val navController = rememberNavController()

    val bottomNavigationItems = listOf(
        BottomNavigationScreens.Home,
        BottomNavigationScreens.Analytics,
        BottomNavigationScreens.History,
        BottomNavigationScreens.Coach,
        BottomNavigationScreens.Settings,
    )
    val snackbarHostState = remember { SnackbarHostState() }

    var fabAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showFab by remember { mutableStateOf(false) }
    var fabTone by remember { mutableStateOf(ElapsedTone.Urgent) }

    if (inAppUpdatePrompt != null) {
        InAppUpdateDialog(
            prompt = inAppUpdatePrompt,
            onDismiss = onDismissUpdatePrompt,
            onConfirm = onStartUpdate,
        )
    }

    if (restartUpdateReady) {
        DownloadedUpdateDialog(
            onDismiss = onDismissRestartPrompt,
            onConfirm = onCompleteDownloadedUpdate,
        )
    }

    Scaffold(
        bottomBar = { BottomNavigation(navController, bottomNavigationItems) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab && currentRoute(navController) == BottomNavigationScreens.Home.route,
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 }),
            ) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.testTag(BUTTON_ADD_SMOKE),
                    onClick = { fabAction?.invoke() },
                    containerColor = fabTone.buttonContainerColor(),
                    contentColor = fabTone.contentColor(),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp,
                        focusedElevation = 6.dp,
                        hoveredElevation = 6.dp,
                    ),
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(com.feragusper.smokeanalytics.features.home.presentation.R.drawable.ic_cigarette),
                            contentDescription = null,
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(com.feragusper.smokeanalytics.features.home.presentation.R.string.home_button_track),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                )
            }
        }
    ) { innerPadding ->
        MainScreenNavigationConfigurations(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            navigateToAuthentication = navigateToAuthentication,
            onFabConfigChanged = { isVisible, tone, action ->
                showFab = isVisible
                fabTone = tone
                fabAction = action
            },
        )
    }
}

@Composable
private fun InAppUpdateDialog(
    prompt: InAppUpdatePrompt,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update available") },
        text = {
            Text(
                buildString {
                    append("A newer Android build is ready in Google Play.")
                    prompt.stalenessDays?.takeIf { it > 0 }?.let { days ->
                        append(" This version has been available for $days day")
                        append(if (days == 1) "." else "s.")
                    }
                    if (prompt.priority > 0) {
                        append(" Update priority: ${prompt.priority}.")
                    }
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Update now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        },
    )
}

@Composable
private fun DownloadedUpdateDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restart to finish update") },
        text = {
            Text("The update has finished downloading. Restart the app to install the latest version.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Restart now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        },
    )
}

private data class InAppUpdatePrompt(
    val availableVersionCode: Int,
    val stalenessDays: Int?,
    val priority: Int,
)

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
    val route = currentRoute(navController)
    val selectedIndex = items.indexOfFirst { it.route == route }.takeIf { it >= 0 } ?: 0

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
 */
@Composable
private fun MainScreenNavigationConfigurations(
    navController: NavHostController,
    modifier: Modifier,
    navigateToAuthentication: () -> Unit,
    onFabConfigChanged: (Boolean, ElapsedTone, (() -> Unit)?) -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = BottomNavigationScreens.Home.route
    ) {
        composable(route = BottomNavigationScreens.Home.route) {
            HomeMobileDestination(
                navigateToAuthentication = navigateToAuthentication,
                navigateToSettings = {
                    navController.navigate(BottomNavigationScreens.Settings.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                navigateToHistory = {
                    navController.navigate(BottomNavigationScreens.History.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onFabConfigChanged = onFabConfigChanged,
            )
        }
        composable(route = BottomNavigationScreens.Analytics.route) {
            onFabConfigChanged(false, ElapsedTone.Urgent, null)
            AnalyticsMobileDestination()
        }
        composable(route = BottomNavigationScreens.History.route) {
            onFabConfigChanged(false, ElapsedTone.Urgent, null)
            HistoryMobileDestination(
                navigateToAuthentication = navigateToAuthentication,
            )
        }
        composable(route = BottomNavigationScreens.Coach.route) {
            onFabConfigChanged(false, ElapsedTone.Urgent, null)
            CoachMobileDestination()
        }
        composable(route = BottomNavigationScreens.Settings.route) {
            onFabConfigChanged(false, ElapsedTone.Urgent, null)
            SettingsMobileDestination()
        }
    }
}

@Composable
private fun ElapsedTone.containerColor() = when (this) {
    ElapsedTone.Urgent -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f)
    ElapsedTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.64f)
    ElapsedTone.Caution -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.56f)
    ElapsedTone.Calm -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)
}

@Composable
private fun ElapsedTone.contentColor() = when (this) {
    ElapsedTone.Urgent -> MaterialTheme.colorScheme.onErrorContainer
    ElapsedTone.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
    ElapsedTone.Caution -> MaterialTheme.colorScheme.onSecondaryContainer
    ElapsedTone.Calm -> MaterialTheme.colorScheme.onPrimaryContainer
}

@Composable
private fun ElapsedTone.buttonContainerColor() = when (this) {
    ElapsedTone.Urgent -> MaterialTheme.colorScheme.errorContainer
    ElapsedTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer
    ElapsedTone.Caution -> MaterialTheme.colorScheme.secondaryContainer
    ElapsedTone.Calm -> MaterialTheme.colorScheme.primaryContainer
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
    return navBackStackEntry?.destination?.route
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
    data object Home : BottomNavigationScreens(route = "home", iconId = R.drawable.ic_home)

    /**
     * The Analytics screen, combining trend analysis and map insights.
     */
    data object Analytics : BottomNavigationScreens(
        route = "analytics",
        iconId = R.drawable.ic_stats
    )

    data object History : BottomNavigationScreens(
        route = "history",
        iconId = R.drawable.ic_history,
    )

    data object Coach : BottomNavigationScreens(route = "coach", iconId = R.drawable.ic_chatbot)

    /**
     * The Settings screen, allowing users to configure app settings and preferences.
     */
    data object Settings : BottomNavigationScreens(route = "settings", iconId = R.drawable.ic_settings)
}

@Preview(showBackground = true)
@Composable
private fun MainContainerScreenPreview() {
    SmokeAnalyticsTheme {
        MainContainerScreen(
            inAppUpdatePrompt = null,
            restartUpdateReady = false,
            onDismissUpdatePrompt = {},
            onStartUpdate = {},
            onDismissRestartPrompt = {},
            onCompleteDownloadedUpdate = {},
            navigateToAuthentication = {},
        )
    }
}
