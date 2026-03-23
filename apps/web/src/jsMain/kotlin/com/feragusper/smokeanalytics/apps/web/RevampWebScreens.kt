package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.settings.presentation.web.SettingsWebDependencies
import com.feragusper.smokeanalytics.features.settings.presentation.web.SettingsWebScreen
import com.feragusper.smokeanalytics.features.stats.presentation.web.StatsWebDependencies
import com.feragusper.smokeanalytics.features.stats.presentation.web.StatsWebScreen
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.dom.Div

@Composable
fun AnalyticsWebScreen(
    statsDeps: StatsWebDependencies,
    mapContent: @Composable () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(AnalyticsTab.Trends) }

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Analytics & Map",
            eyebrow = "Patterns",
            subtitle = "Review smoking frequency and geographic clusters from one destination.",
            badgeText = selectedTab.label,
        )

        Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
            AnalyticsTab.entries.forEach { tab ->
                if (selectedTab == tab) {
                    PrimaryButton(text = tab.label, onClick = {})
                } else {
                    GhostButton(
                        text = tab.label,
                        onClick = { selectedTab = tab },
                    )
                }
            }
        }

        when (selectedTab) {
            AnalyticsTab.Trends -> StatsWebScreen(deps = statsDeps)
            AnalyticsTab.Map -> mapContent()
        }
    }
}

@Composable
fun SettingsAboutWebScreen(
    settingsDeps: SettingsWebDependencies,
    onShare: suspend () -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Settings & About",
            eyebrow = "Preferences",
            subtitle = "Manage your account, app behavior, and product details in one place.",
            actions = {
                PrimaryButton(
                    text = "Share",
                    onClick = { GlobalScope.promise { onShare() } },
                )
            },
        )

        SettingsWebScreen(deps = settingsDeps)
        AboutWebSections()
    }
}

private enum class AnalyticsTab(val label: String) {
    Trends("Trends"),
    Map("Map"),
}
