package com.feragusper.smokeanalytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.feragusper.smokeanalytics.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.rememberNavController
import com.feragusper.smokeanalytics.features.goals.presentation.GoalsConfigureView
import com.feragusper.smokeanalytics.features.goals.presentation.GoalsView
import com.feragusper.smokeanalytics.features.goals.presentation.GoalsViewModel
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
import com.feragusper.smokeanalytics.features.settings.presentation.navigation.SettingsNavigator
import com.feragusper.smokeanalytics.features.stats.presentation.StatsView
import com.feragusper.smokeanalytics.features.stats.presentation.StatsViewModel
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose.HeaderNavigation
import com.feragusper.smokeanalytics.features.stats.presentation.R as StatsR
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose.StatsViewState
import com.feragusper.smokeanalytics.features.stats.presentation.navigation.StatsNavigator
import com.feragusper.smokeanalytics.map.MapMobileRoute
import java.time.LocalDate as JavaLocalDate

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

@Composable
fun AnalyticsMobileDestination(
    active: Boolean,
) {
    var selectedTab by remember { mutableStateOf(AnalyticsTab.Trends) }
    var refreshNonce by remember { mutableStateOf(0) }
    var currentPeriod by remember { mutableStateOf(StatsViewState.StatsPeriod.WEEK) }
    var selectedDate by remember { mutableStateOf(JavaLocalDate.now()) }

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

        if (selectedTab == AnalyticsTab.Trends) {
            PrimaryTabRow(selectedTabIndex = currentPeriod.ordinal) {
                StatsViewState.StatsPeriod.entries.forEach { period ->
                    Tab(
                        selected = currentPeriod == period,
                        onClick = { currentPeriod = period },
                        text = { Text(stringResource(period.tabLabelRes())) },
                    )
                }
            }

            HorizontalDivider()

            HeaderNavigation(
                currentPeriod = currentPeriod,
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
            )

            HorizontalDivider()
        }

        PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
            AnalyticsTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(stringResource(tab.labelRes)) },
                )
            }
        }

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
            )
        }
    }
}

@Composable
fun SettingsMobileDestination() {
    val viewModel = koinViewModel<SettingsViewModel>()
    val navController = rememberNavController()
    viewModel.navigator = remember(navController) { SettingsNavigator(navController) }
    SettingsView(viewModel = viewModel)
}

@Composable
fun GoalsMobileDestination(
    navigateToConfigure: () -> Unit,
) {
    val viewModel = koinViewModel<GoalsViewModel>()
    viewModel.navigator = remember { GoalsNavigator() }
    GoalsView(
        viewModel = viewModel,
        navigateToConfigure = navigateToConfigure,
    )
}

@Composable
fun GoalsConfigureMobileDestination(
    navigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<GoalsViewModel>()
    viewModel.navigator = remember(navigateBack) { GoalsNavigator(navigateBack) }
    GoalsConfigureView(
        viewModel = viewModel,
        navigateBack = navigateBack,
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
