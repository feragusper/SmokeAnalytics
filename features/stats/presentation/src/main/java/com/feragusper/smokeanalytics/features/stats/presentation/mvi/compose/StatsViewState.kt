package com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase.PeriodType
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.time.LocalDate
import java.util.Locale

/**
 * Represents the state of the Stats screen, encapsulating all UI-related data.
 */
data class StatsViewState(
    val stats: SmokeStats? = null,
) : MVIViewState<StatsIntent> {

    enum class StatsPeriod { DAY, WEEK, MONTH, YEAR }

    @Composable
    fun Compose(intent: (StatsIntent) -> Unit) {
        var currentPeriod by remember { mutableStateOf(StatsPeriod.WEEK) }
        var selectedDate by remember { mutableStateOf(LocalDate.now()) }

        LaunchedEffect(currentPeriod, selectedDate) {
            intent(
                StatsIntent.LoadStats(
                    selectedDate.year,
                    selectedDate.month.value,
                    selectedDate.dayOfMonth,
                    currentPeriod.toDomainPeriodType()
                )
            )
        }

        ProvideVicoTheme(rememberSmokeAnalyticsVicoTheme()) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabRow(
                    modifier = Modifier.padding(vertical = 16.dp),
                    selectedTabIndex = currentPeriod.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier
                                .tabIndicatorOffset(tabPositions[currentPeriod.ordinal]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    StatsPeriod.entries.forEach { period ->
                        val isSelected = currentPeriod == period
                        Tab(
                            modifier = Modifier.padding(vertical = 16.dp),
                            selected = isSelected,
                            onClick = {
                                currentPeriod = period
                                intent(
                                    StatsIntent.LoadStats(
                                        year = selectedDate.year,
                                        month = selectedDate.month.value,
                                        day = selectedDate.dayOfMonth,
                                        period = period.toDomainPeriodType()
                                    )
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Text(
                                text = period.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                HeaderNavigation(
                    currentPeriod = currentPeriod,
                    selectedDate = selectedDate,
                    onDateChange = { newDate ->
                        selectedDate = newDate
                        intent(
                            StatsIntent.LoadStats(
                                year = newDate.year,
                                month = newDate.month.value,
                                day = newDate.dayOfMonth,
                                period = currentPeriod.toDomainPeriodType()
                            )
                        )
                    }
                )

                stats?.let {
                    when (currentPeriod) {
                        StatsPeriod.DAY -> LineChart(stats.hourly)
                        StatsPeriod.WEEK -> BarChart(stats.weekly)
                        StatsPeriod.MONTH -> BarChart(stats.monthly)
                        StatsPeriod.YEAR -> BarChart(stats.yearly)
                    }
                }
            }
        }
    }
}

/**
 * Header Navigation to move between days, weeks, months, and years.
 */
@Composable
fun HeaderNavigation(
    currentPeriod: StatsViewState.StatsPeriod,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            onDateChange(
                when (currentPeriod) {
                    StatsViewState.StatsPeriod.DAY -> selectedDate.minusDays(1)
                    StatsViewState.StatsPeriod.WEEK -> selectedDate.minusWeeks(1)
                    StatsViewState.StatsPeriod.MONTH -> selectedDate.minusMonths(1)
                    StatsViewState.StatsPeriod.YEAR -> selectedDate.minusYears(1)
                }
            )
        }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
        }

        Text(
            text = when (currentPeriod) {
                StatsViewState.StatsPeriod.DAY -> selectedDate.toString()
                StatsViewState.StatsPeriod.WEEK -> "Week of $selectedDate"
                StatsViewState.StatsPeriod.MONTH -> selectedDate.month.getDisplayName(
                    java.time.format.TextStyle.FULL,
                    Locale.getDefault()
                )

                StatsViewState.StatsPeriod.YEAR -> selectedDate.year.toString()
            },
            style = MaterialTheme.typography.bodyLarge
        )

        IconButton(onClick = {
            onDateChange(
                when (currentPeriod) {
                    StatsViewState.StatsPeriod.DAY -> selectedDate.plusDays(1)
                    StatsViewState.StatsPeriod.WEEK -> selectedDate.plusWeeks(1)
                    StatsViewState.StatsPeriod.MONTH -> selectedDate.plusMonths(1)
                    StatsViewState.StatsPeriod.YEAR -> selectedDate.plusYears(1)
                }
            )
        }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
        }
    }
}

@Composable
private fun BarChart(stats: Map<String, Int>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val xAxisFormatter = rememberXAxisFormatter(stats)

    LaunchedEffect(stats) {
        modelProducer.runTransaction {
            columnSeries {
                series(stats.values.map { it.toFloat() })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = xAxisFormatter
            ),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}


@Composable
private fun LineChart(stats: Map<String, Int>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val xAxisFormatter = rememberXAxisFormatter(stats)

    val accumulatedValues = stats.values.runningFold(0) { sum, value -> sum + value }.drop(1)

    LaunchedEffect(stats) {
        modelProducer.runTransaction {
            lineSeries {
                series(accumulatedValues.map { it.toFloat() })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = xAxisFormatter
            ),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}

@Composable
fun rememberSmokeAnalyticsVicoTheme(): VicoTheme {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val outline = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onBackground
    val errorColor = MaterialTheme.colorScheme.error

    return remember(primary, secondary, tertiary, outline, textColor, errorColor) {
        VicoTheme(
            candlestickCartesianLayerColors = VicoTheme.CandlestickCartesianLayerColors(
                bullish = primary,
                bearish = errorColor,
                neutral = secondary
            ),
            columnCartesianLayerColors = listOf(primary, secondary, tertiary),
            lineCartesianLayerColors = listOf(primary, secondary, tertiary),
            lineColor = outline,
            textColor = textColor
        )
    }
}

@Composable
fun rememberXAxisFormatter(stats: Map<String, Int>): CartesianValueFormatter {
    val labels = stats.keys.toList()
    return remember(labels) {
        CartesianValueFormatter { _, value, _ ->
            labels.getOrNull(value.toInt()) ?: "?"
        }
    }
}


fun StatsViewState.StatsPeriod.toDomainPeriodType(): PeriodType {
    return when (this) {
        StatsViewState.StatsPeriod.DAY -> PeriodType.DAY
        StatsViewState.StatsPeriod.WEEK -> PeriodType.WEEK
        StatsViewState.StatsPeriod.MONTH -> PeriodType.MONTH
        StatsViewState.StatsPeriod.YEAR -> PeriodType.YEAR
    }
}

/**
 * Preview for Stats Screen.
 */
@CombinedPreviews
@Composable
private fun StatsViewPreview() {
    SmokeAnalyticsTheme {
        StatsViewState().Compose {}
    }
}

