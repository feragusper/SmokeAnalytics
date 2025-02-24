package com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.feragusper.smokeanalytics.libraries.chart.barChart.BarChart
import com.feragusper.smokeanalytics.libraries.chart.barChart.model.BarParameters
import com.feragusper.smokeanalytics.libraries.chart.baseComponents.model.GridOrientation
import com.feragusper.smokeanalytics.libraries.chart.lineChart.LineChart
import com.feragusper.smokeanalytics.libraries.chart.lineChart.model.LineParameters
import com.feragusper.smokeanalytics.libraries.chart.lineChart.model.LineType
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase.PeriodType
import java.time.LocalDate
import java.util.Locale

/**
 * Represents the state of the Stats screen, encapsulating all UI-related data.
 */
data class StatsViewState(
    val stats: SmokeStats? = null,
    val period: StatsPeriod = StatsPeriod.WEEK,  // Default to week view
    val selectedDate: LocalDate = LocalDate.now()
) : MVIViewState<StatsIntent> {

    enum class StatsPeriod { DAY, WEEK, MONTH, YEAR }

    @Composable
    override fun Compose(intent: (StatsIntent) -> Unit) {
        var currentPeriod by remember { mutableStateOf(period) }
        var selectedDate by remember { mutableStateOf(selectedDate) }

        LaunchedEffect(currentPeriod, selectedDate) {
            intent(
                StatsIntent.LoadStats(
                    selectedDate.year,
                    selectedDate.month.value,
                    selectedDate.dayOfMonth,
                    when (currentPeriod) {
                        StatsPeriod.DAY -> PeriodType.DAY
                        StatsPeriod.WEEK -> PeriodType.WEEK
                        StatsPeriod.MONTH -> PeriodType.MONTH
                        StatsPeriod.YEAR -> PeriodType.YEAR
                    }
                )
            )
        }

        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabRow(selectedTabIndex = currentPeriod.ordinal) {
                    StatsViewState.StatsPeriod.entries.forEach { period ->
                        Tab(
                            selected = currentPeriod == period,
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
                            }
                        ) { Text(period.name.replaceFirstChar { it.uppercase() }) }
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

                when (currentPeriod) {
                    StatsPeriod.DAY -> stats?.let {
                        LineChart(stats.hourly)
                    }

                    StatsPeriod.WEEK -> stats?.let {
                        BarChart(stats.weekly)
                    }

                    StatsPeriod.MONTH -> stats?.let {
                        BarChart(stats.monthly)
                    }

                    StatsPeriod.YEAR -> stats?.let {
                        BarChart(stats.yearly)
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        BarChart(
            chartParameters = listOf(
                BarParameters(
                    dataName = null,
                    data = stats.values.map { it.toDouble() },
                    barColor = MaterialTheme.colorScheme.primary
                )
            ),
            gridColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            xAxisData = stats.keys.toList(),
            spaceBetweenGroups = 30.dp,
            barWidth = 20.dp,
            barCornerRadius = 12.dp,
        )
    }
}

@Composable
private fun LineChart(stats: Map<String, Int>) {
    val cumulativeStats = stats.entries
        .scan("00:00" to 0) { acc, entry ->
            entry.key to acc.second + entry.value
        }
        .drop(1)

    val reducedCumulativeStats = cumulativeStats
        .chunked(6) { chunk ->
            val firstLabel = chunk.first().first
            val totalValue = chunk.last().second
            firstLabel to totalValue
        }

    val lastValue = reducedCumulativeStats.lastOrNull()?.second ?: 0
    val adjustedCumulativeStats = reducedCumulativeStats.toMutableList().apply {
        if (last().first != "24:00") {
            add("24:00" to lastValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        LineChart(
            linesParameters = listOf(
                LineParameters(
                    data = adjustedCumulativeStats.map { it.second.toDouble() },
                    lineColor = MaterialTheme.colorScheme.primary,
                    lineType = LineType.CURVED_LINE,
                    lineShadow = true,
                )
            ),
            gridColor = MaterialTheme.colorScheme.secondary,
            xAxisData = adjustedCumulativeStats.map { it.first },
            gridOrientation = GridOrientation.VERTICAL
        )
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
