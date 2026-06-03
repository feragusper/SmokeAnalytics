package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.feragusper.smokeanalytics.features.settings.presentation.web.SettingsWebDependencies
import com.feragusper.smokeanalytics.features.settings.presentation.web.SettingsWebScreen
import com.feragusper.smokeanalytics.features.stats.presentation.web.StatsPeriod
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text

@Composable
fun AnalyticsWebScreen(
    selectedTab: AnalyticsTab,
    selectedPeriod: StatsPeriod,
    selectedDate: LocalDate,
    onSelectTab: (AnalyticsTab) -> Unit,
    onPeriodChange: (StatsPeriod) -> Unit,
    onDateChange: (LocalDate) -> Unit,
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

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.analyticsDateBand) }) {
                GhostButton(
                    text = "←",
                    onClick = { onDateChange(selectedDate.shift(selectedPeriod, -1)) },
                )
                Div(attrs = { classes(SmokeWebStyles.analyticsDateLabel) }) {
                    Text(selectedDate.headerLabel(selectedPeriod))
                }
                GhostButton(
                    text = "→",
                    onClick = { onDateChange(selectedDate.shift(selectedPeriod, +1)) },
                )
                Div(attrs = { classes(SmokeWebStyles.analyticsDateInputSlot) }) {
                    Input(
                        type = InputType.Date,
                        attrs = {
                            value(selectedDate.toHtmlDate())
                            onInput { e ->
                                onDateChange(e.value.toLocalDateOrNull() ?: return@onInput)
                            }
                            classes(SmokeWebStyles.dateInput)
                        }
                    )
                }
            }
            Div(attrs = { classes(SmokeWebStyles.analyticsPeriodBand) }) {
                StatsPeriod.entries.forEach { period ->
                    if (period == selectedPeriod) {
                        PrimaryButton(text = period.label(), onClick = {})
                    } else {
                        GhostButton(
                            text = period.label(),
                            onClick = { onPeriodChange(period) },
                        )
                    }
                }
            }
        }

        Div(attrs = { classes(SmokeWebStyles.analyticsTabBand) }) {
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

private fun StatsPeriod.label(): String = when (this) {
    StatsPeriod.DAY -> "Day"
    StatsPeriod.WEEK -> "Week"
    StatsPeriod.MONTH -> "Month"
    StatsPeriod.YEAR -> "Year"
}

private fun LocalDate.shift(period: StatsPeriod, amount: Int): LocalDate {
    val unit = when (period) {
        StatsPeriod.DAY -> DateTimeUnit.DAY
        StatsPeriod.WEEK -> DateTimeUnit.WEEK
        StatsPeriod.MONTH -> DateTimeUnit.MONTH
        StatsPeriod.YEAR -> DateTimeUnit.YEAR
    }
    return plus(amount, unit)
}

private fun LocalDate.headerLabel(period: StatsPeriod): String = when (period) {
    StatsPeriod.DAY -> toUiDate()
    StatsPeriod.WEEK -> "Week of ${toUiDate()}"
    StatsPeriod.MONTH -> "${monthNumber.toString().padStart(2, '0')}/$year"
    StatsPeriod.YEAR -> year.toString()
}

private fun LocalDate.toHtmlDate(): String {
    val y = year.toString().padStart(4, '0')
    val m = monthNumber.toString().padStart(2, '0')
    val d = dayOfMonth.toString().padStart(2, '0')
    return "$y-$m-$d"
}

private fun LocalDate.toUiDate(): String {
    val d = dayOfMonth.toString().padStart(2, '0')
    val m = monthNumber.toString().padStart(2, '0')
    return "$d/$m/$year"
}

private fun String.toLocalDateOrNull(): LocalDate? {
    if (length != 10) return null
    val y = substring(0, 4).toIntOrNull() ?: return null
    val m = substring(5, 7).toIntOrNull() ?: return null
    val d = substring(8, 10).toIntOrNull() ?: return null
    return runCatching { LocalDate(y, m, d) }.getOrNull()
}
