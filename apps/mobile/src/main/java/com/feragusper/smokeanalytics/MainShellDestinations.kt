package com.feragusper.smokeanalytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.feragusper.smokeanalytics.R
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.authentication.presentation.compose.SignedOutState
import androidx.navigation.compose.rememberNavController
import com.feragusper.smokeanalytics.features.goals.presentation.GoalsView
import com.feragusper.smokeanalytics.features.goals.presentation.GoalsViewModel
import com.feragusper.smokeanalytics.features.goals.presentation.mvi.GoalsIntent
import com.feragusper.smokeanalytics.features.goals.presentation.navigation.GoalsNavigator
import com.feragusper.smokeanalytics.features.history.presentation.HistoryView
import com.feragusper.smokeanalytics.features.history.presentation.HistoryViewModel
import com.feragusper.smokeanalytics.features.history.presentation.navigation.HistoryNavigator
import com.feragusper.smokeanalytics.features.home.domain.ElapsedTone
import com.feragusper.smokeanalytics.features.home.presentation.HomeView
import com.feragusper.smokeanalytics.features.home.presentation.HomeViewModel
import com.feragusper.smokeanalytics.features.home.presentation.navigation.HomeNavigator
import com.feragusper.smokeanalytics.features.settings.presentation.SettingsView
import com.feragusper.smokeanalytics.features.settings.presentation.SettingsViewModel
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator
import com.feragusper.smokeanalytics.features.stats.presentation.StatsView
import com.feragusper.smokeanalytics.features.stats.presentation.StatsViewModel
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose.HeaderNavigation
import com.feragusper.smokeanalytics.features.stats.presentation.R as StatsR
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose.StatsViewState
import com.feragusper.smokeanalytics.features.stats.presentation.navigation.StatsNavigator
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeMapPeriod
import com.feragusper.smokeanalytics.map.MapMobileRoute
import java.time.LocalDate as JavaLocalDate
import kotlinx.datetime.LocalDate as KotlinLocalDate

@Composable
fun HomeMobileDestination(
    active: Boolean,
    navigateToAuthentication: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToHistory: () -> Unit,
    onFabConfigChanged: (Boolean, ElapsedTone, (() -> Unit)?) -> Unit,
) {
    val viewModel = koinViewModel<HomeViewModel>()
    viewModel.navigator = remember(navigateToAuthentication, navigateToSettings, navigateToHistory) {
        HomeNavigator(
            navigateToAuthentication = navigateToAuthentication,
            navigateToSettings = navigateToSettings,
            navigateToHistory = navigateToHistory,
        )
    }
    LaunchedEffect(active) {
        if (active) viewModel.onScreenVisible()
    }

    HomeView(
        viewModel = viewModel,
        onFabConfigChanged = onFabConfigChanged,
    )
}

@Composable
fun HistoryMobileDestination(
    active: Boolean,
    navigateToAuthentication: () -> Unit,
) {
    val viewModel = koinViewModel<HistoryViewModel>()
    viewModel.navigator = remember(navigateToAuthentication) {
        HistoryNavigator(
            navigateToAuthentication = navigateToAuthentication,
            navigateUp = {},
        )
    }
    LaunchedEffect(active) {
        if (active) viewModel.onScreenVisible()
    }

    HistoryView(
        viewModel = viewModel,
        showNavigationIcon = false,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsMobileDestination(
    active: Boolean,
) {
    var selectedTab by remember { mutableStateOf(AnalyticsTab.Trends) }
    var refreshNonce by remember { mutableStateOf(0) }
    var currentPeriod by remember { mutableStateOf(StatsViewState.StatsPeriod.WEEK) }
    var selectedDate by remember { mutableStateOf(JavaLocalDate.now()) }

    val fetchSession = koinInject<FetchSessionUseCase>()
    var signedIn by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(active, refreshNonce) {
        signedIn = fetchSession() is Session.LoggedIn
    }

    LaunchedEffect(active, selectedTab) {
        if (active) refreshNonce += 1
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.map_analytics_and_map),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.map_analytics_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (signedIn == false) {
            SignedOutState(
                modifier = Modifier.fillMaxSize(),
                icon = Icons.Filled.Insights,
                title = stringResource(R.string.analytics_need_account),
                message = stringResource(R.string.analytics_signed_out_body),
                onSignInSuccess = { refreshNonce += 1 },
                onSignInError = {},
            )
            return@Column
        }

        // Frequency / clusters — a segmented control on top of everything, kept on its own (not a
        // tab row, so it doesn't read as the same thing as the period tabs below).
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            AnalyticsTab.entries.forEachIndexed { index, tab ->
                SegmentedButton(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = AnalyticsTab.entries.size,
                    ),
                    // No check icon on select, so the label doesn't shift when chosen.
                    icon = {},
                ) {
                    Text(stringResource(tab.labelRes))
                }
            }
        }

        // Period switcher (day/week/month/year) + the back/forward navigator, joined as one
        // component with no gap: the navigator has no rounded corners so it sits flush under the
        // period tabs. Shared by both tabs, so Clusters navigates the same way as Frequency.
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryTabRow(selectedTabIndex = currentPeriod.ordinal) {
            StatsViewState.StatsPeriod.entries.forEach { period ->
                Tab(
                    selected = currentPeriod == period,
                    onClick = { currentPeriod = period },
                    text = { Text(stringResource(period.tabLabelRes())) },
                )
            }
        }
        HeaderNavigation(
            currentPeriod = currentPeriod,
            selectedDate = selectedDate,
            onDateChange = { selectedDate = it },
        )

        when (selectedTab) {
            AnalyticsTab.Trends -> StatsMobileDestination(
                modifier = Modifier.fillMaxSize(),
                refreshNonce = refreshNonce,
                currentPeriod = currentPeriod,
                selectedDate = selectedDate,
                onPeriodChange = { currentPeriod = it },
                onDateChange = { selectedDate = it },
            )
            AnalyticsTab.Map -> MapMobileRoute(
                modifier = Modifier.fillMaxSize(),
                refreshNonce = refreshNonce,
                embedded = true,
                period = currentPeriod.toSmokeMapPeriod(),
                selectedDate = selectedDate.toKotlinLocalDate(),
            )
        }
    }
}

@Composable
fun SettingsMobileDestination() {
    val viewModel = koinViewModel<SettingsViewModel>()
    val navController = rememberNavController()
    viewModel.navigator = remember(navController) { SettingsNavigator(navController) }

    // Refetch when the tab resumes so edits made in the standalone detail activities show up.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.intents().trySend(SettingsIntent.FetchUser)
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    SettingsView(viewModel = viewModel)
}

@Composable
fun GoalsMobileDestination(
    navigateToConfigure: () -> Unit,
) {
    val viewModel = koinViewModel<GoalsViewModel>()
    viewModel.navigator = remember { GoalsNavigator() }

    // Refetch when the tab resumes so edits made in the standalone editor Activity show up.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.intents().trySend(GoalsIntent.FetchGoals)
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    GoalsView(
        viewModel = viewModel,
        navigateToConfigure = navigateToConfigure,
    )
}

@Composable
private fun StatsMobileDestination(
    modifier: Modifier = Modifier,
    refreshNonce: Int = 0,
    currentPeriod: StatsViewState.StatsPeriod = StatsViewState.StatsPeriod.WEEK,
    selectedDate: JavaLocalDate = JavaLocalDate.now(),
    onPeriodChange: (StatsViewState.StatsPeriod) -> Unit = {},
    onDateChange: (JavaLocalDate) -> Unit = {},
) {
    val viewModel = koinViewModel<StatsViewModel>()
    viewModel.navigator = remember { StatsNavigator() }
    Column(modifier = modifier) {
        StatsView(
            viewModel = viewModel,
            refreshNonce = refreshNonce,
            embedded = true,
            currentPeriod = currentPeriod,
            selectedDate = selectedDate,
            onPeriodChange = onPeriodChange,
            onDateChange = onDateChange,
        )
    }
}

private enum class AnalyticsTab(val labelRes: Int) {
    Trends(R.string.map_tab_frequency),
    Map(R.string.map_tab_clusters),
}

/** Period tab labels for the Analytics shell; reuses the stats module's localized strings. */
private fun StatsViewState.StatsPeriod.tabLabelRes(): Int = when (this) {
    StatsViewState.StatsPeriod.DAY -> StatsR.string.stats_period_day
    StatsViewState.StatsPeriod.WEEK -> StatsR.string.stats_period_week
    StatsViewState.StatsPeriod.MONTH -> StatsR.string.stats_period_month
    StatsViewState.StatsPeriod.YEAR -> StatsR.string.stats_period_year
}

/** Maps the shell's period to the map module's parallel period, so Clusters shares the tabs. */
private fun StatsViewState.StatsPeriod.toSmokeMapPeriod(): SmokeMapPeriod = when (this) {
    StatsViewState.StatsPeriod.DAY -> SmokeMapPeriod.Day
    StatsViewState.StatsPeriod.WEEK -> SmokeMapPeriod.Week
    StatsViewState.StatsPeriod.MONTH -> SmokeMapPeriod.Month
    StatsViewState.StatsPeriod.YEAR -> SmokeMapPeriod.Year
}

private fun JavaLocalDate.toKotlinLocalDate(): KotlinLocalDate =
    KotlinLocalDate(year, monthValue, dayOfMonth)
