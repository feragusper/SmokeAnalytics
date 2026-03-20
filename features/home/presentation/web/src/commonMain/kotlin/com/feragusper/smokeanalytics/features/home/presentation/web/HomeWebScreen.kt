package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeWebStore
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.InlineErrorCard
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonList
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.StatCard
import com.feragusper.smokeanalytics.libraries.design.StatusTone
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import com.feragusper.smokeanalytics.libraries.preferences.domain.formatMoney
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun HomeWebScreen(
    deps: HomeWebDependencies,
    onNavigateToHistory: () -> Unit,
) {
    val store = remember(deps) { HomeWebStore(processHolder = deps.homeProcessHolder) }

    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    state.Render(
        onIntent = { intent ->
            when (intent) {
                HomeIntent.OnClickHistory -> onNavigateToHistory()
                else -> store.send(intent)
            }
        }
    )
}

@Composable
fun HomeViewState.Render(
    onIntent: (HomeIntent) -> Unit,
) {
    val showingInitialSkeleton = error == null && (
        displayLoading ||
            (latestSmokes == null &&
                smokesPerDay == null &&
                smokesPerWeek == null &&
                smokesPerMonth == null &&
                timeSinceLastCigarette == null)
    )
    val elapsedCardToneClass = when (elapsedTone) {
        com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Urgent -> SmokeWebStyles.elapsedCardUrgent
        com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Warning -> SmokeWebStyles.elapsedCardWarning
        com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Caution -> SmokeWebStyles.elapsedCardCaution
        com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Calm -> SmokeWebStyles.elapsedCardCalm
    }
    val addSmokeToneClass = when (elapsedTone) {
        com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Urgent -> SmokeWebStyles.buttonPrimaryUrgent
        com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Warning -> SmokeWebStyles.buttonPrimaryWarning
        com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Caution -> SmokeWebStyles.buttonPrimaryCaution
        com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Calm -> SmokeWebStyles.buttonPrimaryCalm
    }

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Daily snapshot",
            eyebrow = "Home",
            badgeText = when {
                displayRefreshLoading -> "Refreshing"
                error != null -> "Needs attention"
                else -> null
            },
            badgeTone = when {
                displayRefreshLoading -> StatusTone.Busy
                error != null -> StatusTone.Error
                else -> StatusTone.Default
            },
            actions = {
                PrimaryButton(
                    text = "Add smoke",
                    onClick = { onIntent(HomeIntent.AddSmoke) },
                    enabled = !displayLoading,
                    extraClass = addSmokeToneClass,
                )
            }
        )

        if (error != null) {
            InlineErrorCard(
                title = if (error == HomeViewState.HomeError.NotLoggedIn) "Session required" else "Could not refresh home",
                message = when (error) {
                    HomeViewState.HomeError.NotLoggedIn -> "Sign in from Settings to sync the latest smoke entries on the web."
                    HomeViewState.HomeError.Generic -> "The home dashboard could not be refreshed. Try again in a moment."
                },
                actionLabel = "Retry",
                onAction = { onIntent(HomeIntent.RefreshFetchSmokes) },
            )
        }

        if (showingInitialSkeleton) {
            Div(attrs = { classes(SmokeWebStyles.skeletonGrid) }) {
                repeat(3) { LoadingSkeletonCard(heightPx = 96, lineWidths = listOf("36%", "64%")) }
            }
            LoadingSkeletonCard(heightPx = 110, lineWidths = listOf("28%", "42%"))
            LoadingSkeletonList(rows = 4)
        } else {
            if (greetingTitle != null || financialSummary != null || rateSummary != null || gamificationSummary != null) {
                SurfaceCard {
                    greetingTitle?.let { title ->
                        Div(attrs = { classes(SmokeWebStyles.pageHeroTitle) }) { Text(title) }
                    }
                    greetingMessage?.let { message ->
                        Div(attrs = { classes(SmokeWebStyles.sectionBody) }) { Text(message) }
                    }
                    if (financialSummary != null || rateSummary != null || gamificationSummary != null) {
                        Div(attrs = { classes(SmokeWebStyles.summaryMetricGrid) }) {
                            financialSummary?.let { summary ->
                                MetricSummary(
                                    label = "Spent today",
                                    value = summary.spentToday.formatMoney(summary.currencySymbol),
                                    meta = "Week ${summary.spentWeek.formatMoney(summary.currencySymbol)}",
                                )
                            }
                            rateSummary?.let { summary ->
                                MetricSummary(
                                    label = "Average gap today",
                                    value = summary.averageIntervalMinutesToday?.toGapLabel() ?: "--",
                                    meta = summary.latestIntervalMinutes?.let { "Latest ${it.toGapLabel()}" },
                                )
                                MetricSummary(
                                    label = "Average pace",
                                    value = "${summary.averageSmokesPerDayWeek.formatOneDecimal()} / day",
                                    meta = "Month ${summary.averageSmokesPerDayMonth.formatOneDecimal()} / day",
                                )
                            }
                            gamificationSummary?.let { summary ->
                                MetricSummary(
                                    label = "Recovery points",
                                    value = summary.points.toString(),
                                    meta = "Next milestone ${summary.nextMilestoneHours}h",
                                )
                            }
                        }
                    }
                    if (canStartNewDay) {
                        Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                            GhostButton(
                                text = "Start new day",
                                onClick = { onIntent(HomeIntent.StartNewDay) },
                                enabled = !displayLoading && !displayRefreshLoading,
                            )
                        }
                    }
                }
            }

            Div(
                attrs = {
                    classes(SmokeWebStyles.statsRow)
                    if (displayRefreshLoading) classes(SmokeWebStyles.surfaceMuted)
                }
            ) {
                StatCard(
                    title = "Today",
                    value = smokesPerDay?.toString() ?: "--",
                    onClick = { onIntent(HomeIntent.OnClickHistory) }
                )
                StatCard(
                    title = "This week",
                    value = smokesPerWeek?.toString() ?: "--",
                    onClick = { onIntent(HomeIntent.OnClickHistory) }
                )
                StatCard(
                    title = "This month",
                    value = smokesPerMonth?.toString() ?: "--",
                    onClick = { onIntent(HomeIntent.OnClickHistory) }
                )
                StatCard(
                    title = "Avg gap today",
                    value = rateSummary?.averageIntervalMinutesToday?.toGapLabel() ?: "--",
                )
            }

            Div(
                attrs = {
                    classes(SmokeWebStyles.sectionHeader)
                    if (displayRefreshLoading) classes(SmokeWebStyles.surfaceMuted)
                }
            ) {
                Div(attrs = { classes(SmokeWebStyles.sectionHeaderText) }) {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                        Text("Since your last cigarette")
                    }
                }
            }

            SurfaceCard(
                *buildList {
                    add(elapsedCardToneClass)
                    if (displayRefreshLoading) add(SmokeWebStyles.surfaceMuted)
                }.toTypedArray()
            ) {
                val since = timeSinceLastCigarette?.let { (h, m) ->
                    buildString {
                        if (h > 0) append("${h}h, ")
                        append("${m}m")
                    }
                } ?: "--"

                Div(attrs = { classes(SmokeWebStyles.sinceValue) }) {
                    Text(since)
                }
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text(
                        when (elapsedTone) {
                            com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Urgent -> "The last interval is still fresh."
                            com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Warning -> "You are close to the recent pace."
                            com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Caution -> "The cadence is opening up."
                            com.feragusper.smokeanalytics.features.home.domain.ElapsedTone.Calm -> "This gap is comfortably above the recent pace."
                        }
                    )
                }
            }

            SurfaceCard {
                Div(attrs = { classes(SmokeWebStyles.sectionHeader) }) {
                    Div(attrs = { classes(SmokeWebStyles.sectionHeaderText) }) {
                        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("History holds the full log") }
                        Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                            Text("Use History for the full list, edits, and cadence-colored smoke entries.")
                        }
                    }
                }
                Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                    GhostButton(
                        text = "Open history",
                        onClick = { onIntent(HomeIntent.OnClickHistory) },
                        enabled = !displayLoading && !displayRefreshLoading,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricSummary(
    label: String,
    value: String,
    meta: String? = null,
    tooltip: String? = null,
) {
    Div(attrs = {
        classes(SmokeWebStyles.summaryMetricCard)
        tooltip?.let { attr("title", it) }
    }) {
        Div(attrs = { classes(SmokeWebStyles.summaryMetricLabel) }) { Text(label) }
        Div(attrs = { classes(SmokeWebStyles.summaryMetricValue) }) { Text(value) }
        meta?.let {
            Div(attrs = { classes(SmokeWebStyles.summaryMetricMeta) }) { Text(it) }
        }
    }
}

internal fun Instant.toDateInputValue(timeZone: TimeZone): String {
    val ldt = toLocalDateTime(timeZone)
    val mm = ldt.monthNumber.toString().padStart(2, '0')
    val dd = ldt.dayOfMonth.toString().padStart(2, '0')
    return "${ldt.year}-$mm-$dd"
}

internal fun Instant.toTimeInputValue(timeZone: TimeZone): String {
    val ldt = toLocalDateTime(timeZone)
    val hh = ldt.hour.toString().padStart(2, '0')
    val mm = ldt.minute.toString().padStart(2, '0')
    return "$hh:$mm"
}

internal fun dateTimeInputsToInstant(
    dateValue: String,
    timeValue: String,
    timeZone: TimeZone,
): Instant {
    val date = LocalDate.parse(dateValue)
    val time = LocalTime.parse(timeValue)
    val ldt = LocalDateTime(
        year = date.year,
        monthNumber = date.monthNumber,
        dayOfMonth = date.dayOfMonth,
        hour = time.hour,
        minute = time.minute,
        second = 0,
        nanosecond = 0,
    )
    return ldt.toInstant(timeZone)
}

private fun Int.toGapLabel(): String = when {
    this >= 60 -> "${this / 60}h ${this % 60}m"
    else -> "${this}m"
}

private fun Double.formatOneDecimal(): String {
    val rounded = (this * 10).toInt() / 10.0
    val whole = rounded.toInt()
    val decimal = ((rounded - whole) * 10).toInt()
    return "$whole.$decimal"
}
