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
import com.feragusper.smokeanalytics.libraries.design.i18n.AppStrings
import com.feragusper.smokeanalytics.libraries.design.i18n.LocalStrings
import com.feragusper.smokeanalytics.libraries.design.StatusTone
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStatsPeriod
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.averageSummary
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
    embedded: Boolean = false,
    onPeriodChange: (StatsPeriod) -> Unit,
    onDateChange: (LocalDate) -> Unit,
) {
    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    StatsWebContent(
        state = state,
        currentPeriod = currentPeriod,
        selectedDate = selectedDate,
        embedded = embedded,
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
    embedded: Boolean,
    onPeriodChange: (StatsPeriod) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onReload: () -> Unit,
) {
    val strings = LocalStrings.current
    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        if (!embedded) {
            PageSectionHeader(
                title = strings.patternsInMotion,
                eyebrow = strings.eyebrowTrends,
                badgeText = when {
                    state.displayRefreshLoading -> strings.refreshing
                    state.displayLoading -> strings.loading
                    state.error != null -> strings.errorBadge
                    else -> currentPeriod.label(strings)
                },
                badgeTone = when {
                    state.displayLoading || state.displayRefreshLoading -> StatusTone.Busy
                    state.error != null -> StatusTone.Error
                    else -> StatusTone.Default
                }
            )
        }

        SurfaceCard {
            if (embedded) {
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text(
                        when {
                            state.displayRefreshLoading -> strings.refreshingFrequency
                            state.error != null && state.stats != null -> strings.freqRefreshFailed
                            else -> selectedDate.summaryLabel(currentPeriod, strings)
                        }
                    )
                }
            }
            if (!embedded) {
                Div(attrs = { classes(SmokeWebStyles.statsToolbar) }) {
                    Div(attrs = { classes(SmokeWebStyles.periodPills) }) {
                        StatsPeriod.entries.forEach { p ->
                            if (p == currentPeriod) {
                                PrimaryButton(
                                    text = p.label(strings),
                                    onClick = { },
                                    enabled = !state.displayLoading,
                                )
                            } else {
                                GhostButton(
                                    text = p.label(strings),
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
                            Text(selectedDate.headerLabel(currentPeriod, strings))
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
        }

        when {
            state.error != null && state.stats == null -> EmptyStateCard(
                title = strings.patternViewUnavailable,
                message = strings.patternViewUnavailableBody,
                actionLabel = strings.tryAgain,
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
                        Text(strings.statsRefreshFailed)
                    }
                }

                Div(attrs = {
                    attr("style", "display:grid;grid-template-columns:1.6fr 1fr;gap:16px;align-items:start;")
                }) {
                    val averageSummary = averageSummaryFor(currentPeriod, stats, selectedDate)
                    // Only a single day shows a raw total; for week/month/year a big cumulative
                    // number (e.g. "1000 cigarettes") is discouraging, so lead with the daily average.
                    val isDay = currentPeriod == StatsPeriod.DAY
                    SurfaceCard {
                        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;min-height:0;") }) {
                            Div(attrs = { attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                                Text(if (isDay) strings.totalFrequency else statsSummaryTitleText(averageSummary.title, strings))
                            }
                            Div(attrs = { attr("style", "display:flex;align-items:flex-end;gap:10px;") }) {
                                Div(attrs = { attr("style", "font-size:48px;font-weight:800;line-height:1;color:var(--sa-color-primary);") }) {
                                    Text(if (isDay) currentPeriod.totalLabel(stats) else averageSummary.value.formatOneDecimal())
                                }
                                Div(attrs = { attr("style", "font-size:14px;color:var(--sa-color-secondary);margin-bottom:6px;") }) {
                                    Text(if (isDay) strings.cigarettes else statsSummarySupportingText(averageSummary.supporting, strings))
                                }
                            }
                            Div(attrs = { attr("style", "font-size:12px;font-weight:700;text-transform:uppercase;color:#7A4A04;") }) {
                                Text(selectedDate.summaryLabel(currentPeriod, strings))
                            }
                        }
                    }

                    Div(attrs = { attr("style", "display:grid;grid-template-rows:auto auto;gap:16px;align-content:start;") }) {
                        if (isDay) {
                            SurfaceCard {
                                Div(attrs = {
                                    attr("style", "display:flex;flex-direction:column;gap:12px;min-height:0;background:var(--sa-color-primary);border-radius:22px;padding:20px;color:var(--sa-color-onPrimary);")
                                }) {
                                    Div {
                                        Div(attrs = { attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;opacity:0.8;") }) {
                                            Text(statsSummaryTitleText(averageSummary.title, strings))
                                        }
                                        Div(attrs = { attr("style", "font-size:40px;font-weight:800;line-height:1;margin-top:8px;") }) {
                                            Text(averageSummary.value.formatOneDecimal())
                                        }
                                    }
                                    Div(attrs = { attr("style", "font-size:13px;opacity:0.75;") }) {
                                        Text(statsSummarySupportingText(averageSummary.supporting, strings))
                                    }
                                }
                            }
                        }

                        SurfaceCard {
                            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;min-height:0;") }) {
                                Div(attrs = { attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                                    Text(strings.peakWindow)
                                }
                                Div(attrs = { attr("style", "font-size:32px;font-weight:800;line-height:1.1;color:var(--sa-color-primary);") }) {
                                    Text(LocalStrings.current.statsBucketLabel(peakBucketFor(currentPeriod, stats)))
                                }
                                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                                    Text(strings.peakWindowBody)
                                }
                            }
                        }
                    }
                }

                SurfaceCard {
                    Div(attrs = { classes(SmokeWebStyles.chartHeader) }) {
                        Text(strings.smokingFrequency)
                    }

                    Div(attrs = { attr("style", "font-size:12px;color:var(--sa-color-secondary);margin-bottom:14px;") }) {
                        Text("${selectedDate.headerLabel(currentPeriod, strings)} • ${currentPeriod.chartTitle(strings)}")
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
                            title = strings.periodToday,
                            data = stats.hourly.toCumulativeHourly()
                        )

                        StatsPeriod.WEEK -> BarChartJs(
                            canvasId = chartId,
                            title = strings.periodWeek,
                            data = stats.weekly.mapKeys { strings.statsBucketLabel(it.key) }
                        )

                        StatsPeriod.MONTH -> BarChartJs(
                            canvasId = chartId,
                            title = strings.periodMonth,
                            data = stats.monthly.mapKeys { strings.statsBucketLabel(it.key) }
                        )

                        StatsPeriod.YEAR -> BarChartJs(
                            canvasId = chartId,
                            title = strings.periodYear,
                            data = stats.yearly.mapKeys { strings.statsBucketLabel(it.key) }
                        )
                    }
                }

                TriggerBreakdownCardWeb(stats = stats)
            }
        }
    }
}

@Composable
private fun TriggerBreakdownCardWeb(
    stats: com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats,
) {
    val strings = LocalStrings.current
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
            Div(attrs = { classes(SmokeWebStyles.chartHeader) }) { Text(strings.byTrigger) }
            val breakdown = stats.triggerBreakdown
            if (breakdown.isEmpty()) {
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text(strings.byTriggerEmpty)
                }
            } else {
                Div(attrs = { attr("style", "max-width:420px;margin:0 auto;") }) {
                    Canvas(attrs = {
                        id(TRIGGER_PIE_CANVAS_ID)
                        attr("width", "420")
                        attr("height", "320")
                    })
                }
                val total = breakdown.sumOf { it.count }.coerceAtLeast(1)
                PieChartJs(
                    canvasId = TRIGGER_PIE_CANVAS_ID,
                    // Bake the share into the legend label so every slice's % is visible.
                    labels = breakdown.map { "${it.label} — ${it.count * 100 / total}%" },
                    values = breakdown.map { it.count },
                )
            }
        }
    }
}

private const val TRIGGER_PIE_CANVAS_ID = "trigger-breakdown-pie"

@Composable
private fun PieChartJs(
    canvasId: String,
    labels: List<String>,
    values: List<Int>,
) {
    val chartHolder = remember { mutableStateOf<Chart?>(null) }

    DisposableEffect(canvasId, labels, values) {
        chartHolder.value?.destroy()

        val ctx = canvas2dContext(canvasId)

        val config = jsObject()
        config["type"] = "pie"

        val dataset = jsObject()
        dataset["data"] = values.map { it as Number }.toTypedArray()
        dataset["backgroundColor"] = PIE_COLORS
        dataset["borderWidth"] = 1

        val dataObj = jsObject()
        dataObj["labels"] = labels.toTypedArray()
        dataObj["datasets"] = arrayOf(dataset)
        config["data"] = dataObj

        val legend = jsObject()
        legend["position"] = "right"
        val plugins = jsObject()
        plugins["legend"] = legend
        val options = jsObject()
        options["responsive"] = true
        options["maintainAspectRatio"] = false
        options["plugins"] = plugins
        config["options"] = options

        chartHolder.value = Chart(ctx, config)

        onDispose {
            chartHolder.value?.destroy()
            chartHolder.value = null
        }
    }
}

private val PIE_COLORS = arrayOf(
    "#006A6A",
    "#1D7B7B",
    "#3A8C8C",
    "#57A0A0",
    "#74B3B3",
    "#91C6C6",
    "#AED9D9",
    "#4A6363",
    "#6D8686",
    "#9AB0B0",
)

private fun StatsPeriod.label(strings: AppStrings): String = when (this) {
    StatsPeriod.DAY -> strings.periodDay
    StatsPeriod.WEEK -> strings.periodWeek
    StatsPeriod.MONTH -> strings.periodMonth
    StatsPeriod.YEAR -> strings.periodYear
}

private fun StatsPeriod.chartTitle(strings: AppStrings): String = when (this) {
    StatsPeriod.DAY -> strings.chartTitleDay
    StatsPeriod.WEEK -> strings.chartTitleWeek
    StatsPeriod.MONTH -> strings.chartTitleMonth
    StatsPeriod.YEAR -> strings.chartTitleYear
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

private fun LocalDate.headerLabel(period: StatsPeriod, strings: AppStrings): String = when (period) {
    StatsPeriod.DAY -> toUiDate()
    StatsPeriod.WEEK -> strings.weekOf(toUiDate())
    StatsPeriod.MONTH -> "${monthNumber.toString().padStart(2, '0')}/$year"
    StatsPeriod.YEAR -> year.toString()
}

private fun LocalDate.summaryLabel(period: StatsPeriod, strings: AppStrings): String = when (period) {
    StatsPeriod.DAY -> strings.selectedDay
    StatsPeriod.WEEK -> strings.weekOf(toUiDate())
    StatsPeriod.MONTH -> strings.monthOverview
    StatsPeriod.YEAR -> strings.yearToDate
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

private fun averageSummaryFor(
    period: StatsPeriod,
    stats: com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats,
    selectedDate: LocalDate,
) = stats.averageSummary(
    period = when (period) {
        StatsPeriod.DAY -> SmokeStatsPeriod.DAY
        StatsPeriod.WEEK -> SmokeStatsPeriod.WEEK
        StatsPeriod.MONTH -> SmokeStatsPeriod.MONTH
        StatsPeriod.YEAR -> SmokeStatsPeriod.YEAR
    },
    selectedDate = selectedDate,
)

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

private fun statsSummaryTitleText(
    title: com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummaryTitle,
    s: com.feragusper.smokeanalytics.libraries.design.i18n.AppStrings,
): String = when (title) {
    com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummaryTitle.AwakeHourPace -> s.statsSummaryAwakeHourPace
    com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummaryTitle.DailyPace -> s.statsSummaryDailyPace
}

private fun statsSummarySupportingText(
    supporting: com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummarySupporting,
    s: com.feragusper.smokeanalytics.libraries.design.i18n.AppStrings,
): String = when (supporting) {
    com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummarySupporting.AwakeHourSoFar -> s.statsSummaryAwakeSoFar
    com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummarySupporting.AwakeHour -> s.statsSummaryAwake
    com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummarySupporting.AcrossWeek -> s.statsSummaryAcrossWeek
    com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummarySupporting.AcrossElapsedWeek -> s.statsSummaryAcrossElapsedWeek
    com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummarySupporting.AcrossMonth -> s.statsSummaryAcrossMonth
    com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummarySupporting.AcrossElapsedMonth -> s.statsSummaryAcrossElapsedMonth
    com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummarySupporting.AcrossYear -> s.statsSummaryAcrossYear
    com.feragusper.smokeanalytics.libraries.smokes.domain.model.StatsSummarySupporting.AcrossElapsedYear -> s.statsSummaryAcrossElapsedYear
}
