package com.feragusper.smokeanalytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.feragusper.smokeanalytics.features.chatbot.presentation.ChatbotView
import com.feragusper.smokeanalytics.features.chatbot.presentation.ChatbotViewModel
import com.feragusper.smokeanalytics.features.chatbot.presentation.navigation.ChatbotNavigator
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
import com.feragusper.smokeanalytics.features.stats.presentation.navigation.StatsNavigator
import com.feragusper.smokeanalytics.map.MapMobileRoute

@Composable
fun HomeMobileDestination(
    navigateToAuthentication: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToHistory: () -> Unit,
    onFabConfigChanged: (Boolean, ElapsedTone, (() -> Unit)?) -> Unit,
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    viewModel.navigator = remember(navigateToAuthentication, navigateToSettings, navigateToHistory) {
        HomeNavigator(
            navigateToAuthentication = navigateToAuthentication,
            navigateToSettings = navigateToSettings,
            navigateToHistory = navigateToHistory,
        )
    }

    HomeView(
        viewModel = viewModel,
        onFabConfigChanged = onFabConfigChanged,
    )
}

@Composable
fun HistoryMobileDestination(
    navigateToAuthentication: () -> Unit,
) {
    val viewModel = hiltViewModel<HistoryViewModel>()
    viewModel.navigator = remember(navigateToAuthentication) {
        HistoryNavigator(
            navigateToAuthentication = navigateToAuthentication,
            navigateUp = {},
        )
    }

    HistoryView(viewModel = viewModel)
}

@Composable
fun AnalyticsMobileDestination() {
    var selectedTab by remember { mutableStateOf(AnalyticsTab.Trends) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Analytics & Map",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Review your trends and the places where smoking clusters show up.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
            AnalyticsTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.label) },
                )
            }
        }

        when (selectedTab) {
            AnalyticsTab.Trends -> StatsMobileDestination(modifier = Modifier.fillMaxSize())
            AnalyticsTab.Map -> MapMobileRoute(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun CoachMobileDestination() {
    val viewModel = hiltViewModel<ChatbotViewModel>()
    viewModel.navigator = remember { ChatbotNavigator() }
    ChatbotView(viewModel = viewModel)
}

@Composable
fun SettingsMobileDestination() {
    val viewModel = hiltViewModel<SettingsViewModel>()
    val navController = rememberNavController()
    viewModel.navigator = remember(navController) { SettingsNavigator(navController) }
    SettingsView(viewModel = viewModel)
}

@Composable
private fun StatsMobileDestination(
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<StatsViewModel>()
    viewModel.navigator = remember { StatsNavigator() }
    Column(modifier = modifier) {
        StatsView(viewModel = viewModel)
    }
}

private enum class AnalyticsTab(val label: String) {
    Trends("Trends"),
    Map("Map"),
}
