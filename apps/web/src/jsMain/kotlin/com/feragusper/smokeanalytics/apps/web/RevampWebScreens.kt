package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.feragusper.smokeanalytics.features.settings.presentation.web.SettingsWebDependencies
import com.feragusper.smokeanalytics.features.settings.presentation.web.SettingsWebScreen
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Div

@Composable
fun AnalyticsWebScreen(
    selectedTab: AnalyticsTab,
    onSelectTab: (AnalyticsTab) -> Unit,
    statsContent: @Composable () -> Unit,
    mapContent: @Composable () -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Analytics & Map",
            eyebrow = "Patterns",
            subtitle = "Review smoking frequency and repeated smoking areas from one destination.",
            badgeText = selectedTab.label,
        )

        Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
            AnalyticsTab.entries.forEach { tab ->
                if (selectedTab == tab) {
                    PrimaryButton(text = tab.label, onClick = {})
                } else {
                    GhostButton(
                        text = tab.label,
                        onClick = { onSelectTab(tab) },
                    )
                }
            }
        }

        when (selectedTab) {
            AnalyticsTab.Trends -> statsContent()
            AnalyticsTab.Map -> mapContent()
        }
    }
}

@Composable
fun SettingsAboutWebScreen(
    settingsDeps: SettingsWebDependencies,
    onShare: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "You",
            eyebrow = "Personal space",
            subtitle = "Keep account, routine preferences, goals, and product details in one calmer destination.",
            actions = {
                PrimaryButton(
                    text = "Share",
                    onClick = { scope.launch { onShare() } },
                )
            },
        )

        SettingsWebScreen(
            deps = settingsDeps,
            onShare = onShare,
        )
    }
}

enum class AnalyticsTab(val label: String) {
    Trends("Frequency"),
    Map("Clusters"),
}
