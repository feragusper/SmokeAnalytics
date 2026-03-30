package com.feragusper.smokeanalytics.features.stats.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.stats.presentation.web.mvi.StatsIntent
import com.feragusper.smokeanalytics.features.stats.presentation.web.mvi.StatsWebStore
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.StatusTone
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Canvas
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text

@Composable
fun StatsWebScreen(
    store: StatsWebStore,
    currentPeriod: StatsPeriod,
    selectedDate: LocalDate,
    onPeriodChange: (StatsPeriod) -> Unit,
    onDateChange: (LocalDate) -> Unit,
) {
    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    StatsWebContent(
        state = state,
        currentPeriod = currentPeriod,
        selectedDate = selectedDate,
        onPeriodChange = onPeriodChange,
        onDateChange = onDateChange,
        onReload = {
            store.send(
                StatsIntent.LoadStats(
                    year = selectedDate.year,
                    month = selectedDate.monthNumber,
                    day = selectedDate.dayOfMonth,
                    period = currentPeriod.toDomainPeriodType(),
                )
            )
        }
    )
}

enum class StatsPeriod { DAY, WEEK, MONTH, YEAR }

fun StatsPeriod.toDomainPeriodType(): FetchSmokeStatsUseCase.PeriodType = when (this) {
    StatsPeriod.DAY -> FetchSmokeStatsUseCase.PeriodType.DAY
    StatsPeriod.WEEK -> FetchSmokeStatsUseCase.PeriodType.WEEK
    StatsPeriod.MONTH -> FetchSmokeStatsUseCase.PeriodType.MONTH
    StatsPeriod.YEAR -> FetchSmokeStatsUseCase.PeriodType.YEAR
}

@Composable
private fun StatsWebContent(
    state: StatsViewState,
    currentPeriod: StatsPeriod,
    selectedDate: LocalDate,
    onPeriodChange: (StatsPeriod) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onReload: () -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Patterns in motion",
            eyebrow = "Trends",
            badgeText = when {
                state.displayRefreshLoading -> "Refreshing"
                state.displayLoading -> "Loading"
                state.error != null -> "Error"
                else -> currentPeriod.label()
            },
            badgeTone = when {
                state.displayLoading || state.displayRefreshLoading -> StatusTone.Busy
                state.error != null -> StatusTone.Error
                else -> StatusTone.Default
            }
        )

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.statsToolbar) }) {
                Div(attrs = { classes(SmokeWebStyles.periodPills) }) {
                    StatsPeriod.entries.forEach { p ->
                        if (p == currentPeriod) {
                            PrimaryButton(
                                text = p.label(),
                                onClick = { },
                                enabled = !state.displayLoading,
                            )
                        } else {
                            GhostButton(
                                text = p.label(),
                                onClick = { onPeriodChange(p) },
                                enabled = !state.displayLoading,
                            )
                        }
                    }
                }

                Div(attrs = { classes(SmokeWebStyles.dateControls) }) {
                    GhostButton(
                        text = "←",
                        onClick = { onDateChange(selectedDate.shift(currentPeriod, -1)) },
                        enabled = !state.displayLoading
                    )

                    Div(attrs = { classes(SmokeWebStyles.dateLabel) }) {
                        Text(selectedDate.headerLabel(currentPeriod))
                    }

                    GhostButton(
                        text = "→",
                        onClick = { onDateChange(selectedDate.shift(currentPeriod, +1)) },
                        enabled = !state.displayLoading
                    )

                    Input(
                        type = InputType.Date,
                        attrs = {
                            value(selectedDate.toHtmlDate())
                            if (state.displayLoading) disabled()
                            onInput { e ->
                                val picked = e.value.toLocalDateOrNull() ?: return@onInput
                                onDateChange(picked)
                            }
                            classes(SmokeWebStyles.dateInput)
                        }
                    )
                }
            }
        }

        when {
            state.error != null && state.stats == null -> EmptyStateCard(
                title = "Pattern view unavailable",
                message = "The selected range could not be assembled right now. Keep the period and date, then refresh to try this view again.",
                actionLabel = "Try again",
                onAction = onReload,
            )

            state.displayLoading && state.stats == null -> LoadingSkeletonCard(
                heightPx = 240,
                lineWidths = listOf("24%", "64%", "42%")
            )

            else -> {
                val stats = state.stats ?: return@Div
                val chartId = remember(currentPeriod) { "statsChart_${currentPeriod.name}" }

                if (state.error != null) {
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text("Latest refresh failed. Showing the last available analytics snapshot.")
                    }
                }

                Div(attrs = {
                    attr("style", "display:grid;grid-template-columns:1.6fr 1fr;gap:16px;align-items:start;")
                }) {
                    SurfaceCard {
                        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;min-height:0;") }) {
                            Div(attrs = { attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                                Text("Total Frequency")
                            }
                            Div(attrs = { attr("style", "display:flex;align-items:flex-end;gap:10px;") }) {
                                Div(attrs = { attr("style", "font-size:48px;font-weight:800;line-height:1;color:var(--sa-color-primary);") }) {
                                    Text(currentPeriod.totalLabel(stats))
                                }
                                Div(attrs = { attr("style", "font-size:14px;color:var(--sa-color-secondary);margin-bottom:6px;") }) {
                                    Text("Cigarettes")
                                }
                            }
                            Div(attrs = { attr("style", "font-size:12px;font-weight:700;text-transform:uppercase;color:#7A4A04;") }) {
                                Text(selectedDate.summaryLabel(currentPeriod))
                            }
                        }
                    }

                    Div(attrs = { attr("style", "display:grid;grid-template-rows:auto auto;gap:16px;align-content:start;") }) {
                        SurfaceCard {
                            Div(attrs = {
                                attr("style", "display:flex;flex-direction:column;gap:12px;min-height:0;background:var(--sa-color-primary);border-radius:22px;padding:20px;color:var(--sa-color-onPrimary);")
                            }) {
                                Div {
                                    Div(attrs = { attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;opacity:0.8;") }) {
                                        Text("Daily Average")
                                    }
                                    Div(attrs = { attr("style", "font-size:40px;font-weight:800;line-height:1;margin-top:8px;") }) {
                                        Text(averageFor(currentPeriod, stats).formatOneDecimal())
                                    }
                                }
                                Div(attrs = { attr("style", "font-size:13px;opacity:0.75;") }) {
                                    Text(averageLabelFor(currentPeriod))
                                }
                            }
                        }

                        SurfaceCard {
                            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;min-height:0;") }) {
                                Div(attrs = { attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                                    Text("Peak Window")
                                }
                                Div(attrs = { attr("style", "font-size:32px;font-weight:800;line-height:1.1;color:var(--sa-color-primary);") }) {
                                    Text(peakBucketFor(currentPeriod, stats))
                                }
                                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                                    Text("Highest activity bucket for the selected range.")
                                }
                            }
                        }
                    }
                }

                SurfaceCard {
                    Div(attrs = { classes(SmokeWebStyles.chartHeader) }) {
                        Text("Smoking Frequency")
                    }

                    Div(attrs = { attr("style", "font-size:12px;color:var(--sa-color-secondary);margin-bottom:14px;") }) {
                        Text("${selectedDate.headerLabel(currentPeriod)} • ${currentPeriod.chartTitle()}")
                    }

                    Div(attrs = { classes(SmokeWebStyles.chartWrap) }) {
                        Canvas(attrs = {
                            id(chartId)
                            attr("width", "1200")
                            attr("height", "420")
                        })
                    }

                    when (currentPeriod) {
                        StatsPeriod.DAY -> LineChartJs(
                            canvasId = chartId,
                            title = "Today",
                            data = stats.hourly.toCumulativeHourly()
                        )

                        StatsPeriod.WEEK -> BarChartJs(
                            canvasId = chartId,
                            title = "Week",
                            data = stats.weekly
                        )

                        StatsPeriod.MONTH -> BarChartJs(
                            canvasId = chartId,
                            title = "Month",
                            data = stats.monthly
                        )

                        StatsPeriod.YEAR -> BarChartJs(
                            canvasId = chartId,
                            title = "Year",
                            data = stats.yearly
                        )
                    }
                }
            }
        }
    }
}

private fun StatsPeriod.label(): String = when (this) {
    StatsPeriod.DAY -> "Day"
    StatsPeriod.WEEK -> "Week"
    StatsPeriod.MONTH -> "Month"
    StatsPeriod.YEAR -> "Year"
}

private fun StatsPeriod.chartTitle(): String = when (this) {
    StatsPeriod.DAY -> "Today (hourly)"
    StatsPeriod.WEEK -> "This week"
    StatsPeriod.MONTH -> "This month"
    StatsPeriod.YEAR -> "This year"
}

private fun StatsPeriod.totalLabel(stats: com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats): String = when (this) {
    StatsPeriod.DAY -> stats.totalDay.toString()
    StatsPeriod.WEEK -> stats.totalWeek.toString()
    StatsPeriod.MONTH -> stats.totalMonth.toString()
    StatsPeriod.YEAR -> stats.yearly.values.sum().toString()
}

@Composable
private fun LineChartJs(
    canvasId: String,
    title: String,
    data: Map<String, Int>,
) {
    val labels = remember(data) { data.keys.toList() }
    val values = remember(data) { data.values.map { it as Number } }
    val chartHolder = remember { mutableStateOf<Chart?>(null) }

    DisposableEffect(canvasId, labels, values) {
        chartHolder.value?.destroy()

        val ctx = canvas2dContext(canvasId)

        val config = jsObject()
        config["type"] = "line"

        val dataObj = jsObject()
        dataObj["labels"] = labels.toTypedArray()
        dataObj["datasets"] = arrayOf(lineDataset(title, values))
        config["data"] = dataObj

        val options = jsObject()
        options["responsive"] = true
        options["maintainAspectRatio"] = false
        config["options"] = options

        chartHolder.value = Chart(ctx, config)

        onDispose {
            chartHolder.value?.destroy()
            chartHolder.value = null
        }
    }
}

@Composable
private fun BarChartJs(
    canvasId: String,
    title: String,
    data: Map<String, Int>,
) {
    val labels = remember(data) { data.keys.toList() }
    val values = remember(data) { data.values.map { it as Number } }
    val chartHolder = remember { mutableStateOf<Chart?>(null) }

    DisposableEffect(canvasId, labels, values) {
        chartHolder.value?.destroy()

        val ctx = canvas2dContext(canvasId)

        val config = jsObject()
        config["type"] = "bar"

        val dataObj = jsObject()
        dataObj["labels"] = labels.toTypedArray()
        dataObj["datasets"] = arrayOf(barDataset(title, values))
        config["data"] = dataObj

        val options = jsObject()
        options["responsive"] = true
        options["maintainAspectRatio"] = false
        config["options"] = options

        chartHolder.value = Chart(ctx, config)

        onDispose {
            chartHolder.value?.destroy()
            chartHolder.value = null
        }
    }
}

private fun LocalDate.shift(period: StatsPeriod, amount: Int): LocalDate {
    val unit = when (period) {
        StatsPeriod.DAY -> DateTimeUnit.DAY
        StatsPeriod.WEEK -> DateTimeUnit.WEEK
        StatsPeriod.MONTH -> DateTimeUnit.MONTH
        StatsPeriod.YEAR -> DateTimeUnit.YEAR
    }
    return this.plus(amount, unit)
}

private fun LocalDate.headerLabel(period: StatsPeriod): String = when (period) {
    StatsPeriod.DAY -> toUiDate()
    StatsPeriod.WEEK -> "Week of ${toUiDate()}"
    StatsPeriod.MONTH -> "${monthNumber.toString().padStart(2, '0')}/$year"
    StatsPeriod.YEAR -> year.toString()
}

private fun LocalDate.summaryLabel(period: StatsPeriod): String = when (period) {
    StatsPeriod.DAY -> "Selected day"
    StatsPeriod.WEEK -> "Week of ${toUiDate()}"
    StatsPeriod.MONTH -> "Month overview"
    StatsPeriod.YEAR -> "Year to date"
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

private fun Map<String, Int>.toCumulativeHourly(): Map<String, Int> {
    var runningTotal = 0
    return entries.associate { (label, value) ->
        runningTotal += value
        label to runningTotal
    }
}

private fun averageFor(period: StatsPeriod, stats: com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats): Double {
    val values = when (period) {
        StatsPeriod.DAY -> stats.hourly.values
        StatsPeriod.WEEK -> stats.weekly.values
        StatsPeriod.MONTH -> stats.monthly.values
        StatsPeriod.YEAR -> stats.yearly.values
    }
    return values.takeIf { it.isNotEmpty() }?.average() ?: 0.0
}

private fun averageLabelFor(period: StatsPeriod): String = when (period) {
    StatsPeriod.DAY -> "Average per hour"
    StatsPeriod.WEEK -> "Average per weekday"
    StatsPeriod.MONTH -> "Average per week bucket"
    StatsPeriod.YEAR -> "Average per month"
}

private fun peakBucketFor(
    period: StatsPeriod,
    stats: com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats,
): String = when (period) {
    StatsPeriod.DAY -> stats.hourly.maxByOrNull { it.value }?.key ?: "--"
    StatsPeriod.WEEK -> stats.weekly.maxByOrNull { it.value }?.key ?: "--"
    StatsPeriod.MONTH -> stats.monthly.maxByOrNull { it.value }?.key ?: "--"
    StatsPeriod.YEAR -> stats.yearly.maxByOrNull { it.value }?.key ?: "--"
}

private fun Double.formatOneDecimal(): String {
    val rounded = (this * 10).toInt() / 10.0
    val whole = rounded.toInt()
    val decimal = ((rounded - whole) * 10).toInt()
    return "$whole.$decimal"
}

private fun jsObject(): dynamic = js("({})")

private fun lineDataset(title: String, values: List<Number>): dynamic {
    val dataset = jsObject()
    dataset["label"] = title
    dataset["data"] = values.toTypedArray()
    dataset["borderColor"] = "#006A6A"
    dataset["backgroundColor"] = "rgba(0,106,106,0.18)"
    dataset["tension"] = 0.3
    dataset["fill"] = true
    return dataset
}

private fun barDataset(title: String, values: List<Number>): dynamic {
    val dataset = jsObject()
    dataset["label"] = title
    dataset["data"] = values.toTypedArray()
    dataset["backgroundColor"] = arrayOf(
        "#006A6A",
        "#1D7B7B",
        "#3A8C8C",
        "#4A6363",
        "#6D8686",
        "#9AB0B0",
        "#B0CCCB",
    )
    dataset["borderRadius"] = 10
    dataset["maxBarThickness"] = 42
    return dataset
}
