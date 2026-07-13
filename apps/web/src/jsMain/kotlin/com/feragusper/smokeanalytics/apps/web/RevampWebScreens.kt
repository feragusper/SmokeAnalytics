package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.features.settings.presentation.web.SettingsWebDependencies
import com.feragusper.smokeanalytics.features.settings.presentation.web.SettingsWebScreen
import com.feragusper.smokeanalytics.features.stats.presentation.web.StatsPeriod
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import com.feragusper.smokeanalytics.libraries.design.i18n.AppStrings
import com.feragusper.smokeanalytics.libraries.design.i18n.LocalStrings
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
    val strings = LocalStrings.current
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = strings.analyticsAndMap,
            eyebrow = strings.eyebrowPatterns,
            subtitle = strings.analyticsAndMapSubtitle,
            badgeText = selectedTab.label(strings),
        )

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.analyticsDateBand) }) {
                GhostButton(
                    text = "←",
                    onClick = { onDateChange(selectedDate.shift(selectedPeriod, -1)) },
                )
                Div(attrs = { classes(SmokeWebStyles.analyticsDateLabel) }) {
                    Text(selectedDate.headerLabel(selectedPeriod, strings))
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
                        PrimaryButton(text = period.label(strings), onClick = {})
                    } else {
                        GhostButton(
                            text = period.label(strings),
                            onClick = { onPeriodChange(period) },
                        )
                    }
                }
            }
        }

        Div(attrs = { classes(SmokeWebStyles.analyticsTabBand) }) {
            AnalyticsTab.entries.forEach { tab ->
                if (selectedTab == tab) {
                    PrimaryButton(text = tab.label(strings), onClick = {})
                } else {
                    GhostButton(
                        text = tab.label(strings),
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
    val strings = LocalStrings.current
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = strings.navYou,
            eyebrow = strings.eyebrowPersonalSpace,
            subtitle = strings.youSubtitle,
        )

        SettingsWebScreen(
            deps = settingsDeps,
            onShare = onShare,
        )
    }
}

enum class AnalyticsTab {
    Trends,
    Map,
}

private fun AnalyticsTab.label(strings: AppStrings): String = when (this) {
    AnalyticsTab.Trends -> strings.tabFrequency
    AnalyticsTab.Map -> strings.tabClusters
}

private fun StatsPeriod.label(strings: AppStrings): String = when (this) {
    StatsPeriod.DAY -> strings.periodDay
    StatsPeriod.WEEK -> strings.periodWeek
    StatsPeriod.MONTH -> strings.periodMonth
    StatsPeriod.YEAR -> strings.periodYear
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

private fun LocalDate.headerLabel(period: StatsPeriod, strings: AppStrings): String = when (period) {
    StatsPeriod.DAY -> toUiDate()
    StatsPeriod.WEEK -> strings.weekOf(toUiDate())
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
