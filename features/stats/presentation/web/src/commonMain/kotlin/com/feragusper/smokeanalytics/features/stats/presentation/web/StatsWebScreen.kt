package com.feragusper.smokeanalytics.features.stats.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.stats.presentation.web.mvi.StatsIntent
import com.feragusper.smokeanalytics.features.stats.presentation.web.mvi.StatsWebStore
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Canvas
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun StatsWebScreen(
    deps: StatsWebDependencies,
) {
    val store = remember(deps) { StatsWebStore(processHolder = deps.processHolder) }
    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    var currentPeriod by remember { mutableStateOf(StatsPeriod.WEEK) }
    var selectedDate by remember {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        mutableStateOf(today)
    }

    LaunchedEffect(currentPeriod, selectedDate) {
        store.send(
            StatsIntent.LoadStats(
                year = selectedDate.year,
                month = selectedDate.monthNumber,
                day = selectedDate.dayOfMonth,
                period = currentPeriod.toDomainPeriodType(),
            )
        )
    }

    StatsWebContent(
        state = state,
        currentPeriod = currentPeriod,
        selectedDate = selectedDate,
        onPeriodChange = { currentPeriod = it },
        onDateChange = { selectedDate = it },
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

private enum class StatsPeriod { DAY, WEEK, MONTH, YEAR }

private fun StatsPeriod.toDomainPeriodType(): FetchSmokeStatsUseCase.PeriodType = when (this) {
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
    Div(attrs = { classes(SmokeWebStyles.mainInner) }) {

        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Stats") }

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.statsToolbar) }) {

                Div(attrs = { classes(SmokeWebStyles.periodPills) }) {
                    StatsPeriod.entries.forEach { p ->
                        val isSelected = p == currentPeriod
                        val label = p.label()

                        if (isSelected) {
                            PrimaryButton(
                                text = label,
                                onClick = { /* no-op */ },
                                enabled = !state.displayLoading
                            )
                        } else {
                            GhostButton(
                                text = label,
                                onClick = { onPeriodChange(p) },
                                enabled = !state.displayLoading
                            )
                        }

                        Span { Text(" ") }
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

                    GhostButton(
                        text = "Reload",
                        onClick = onReload,
                        enabled = !state.displayLoading
                    )
                }
            }
        }

        if (state.displayLoading) {
            SurfaceCard { Text("Loading...") }
        }

        if (state.error != null) {
            SurfaceCard { Text("Something went wrong") }
        }

        state.stats?.let { stats ->
            val chartId = remember(currentPeriod) { "statsChart_${currentPeriod.name}" }

            SurfaceCard {
                Div(attrs = { classes(SmokeWebStyles.chartHeader) }) {
                    Text(currentPeriod.chartTitle())
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
                        data = stats.hourly
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
    StatsPeriod.MONTH -> "${monthNumber.toString().padStart(2, '0')}/${year}"
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

private fun jsObject(): dynamic = js("({})")

private fun lineDataset(
    title: String,
    values: List<Number>,
): dynamic {
    val ds = jsObject()
    ds["label"] = title
    ds["data"] = values.toTypedArray()
    ds["tension"] = 0.25
    ds["borderWidth"] = 2
    ds["pointRadius"] = 2
    ds["fill"] = false
    return ds
}

private fun barDataset(
    title: String,
    values: List<Number>,
): dynamic {
    val ds = jsObject()
    ds["label"] = title
    ds["data"] = values.toTypedArray()
    ds["borderWidth"] = 1
    ds["fill"] = false
    return ds
}