package com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
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
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Patterns in motion",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Compare period totals, pacing, and chart movement without leaving Analytics.",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        TabRow(
                            modifier = Modifier.padding(bottom = 8.dp),
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
                                    modifier = Modifier.padding(vertical = 12.dp),
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
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                stats?.let {
                    SummaryCards(
                        currentPeriod = currentPeriod,
                        stats = stats,
                        selectedDate = selectedDate,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Smoking Frequency",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = selectedDate.analyticsLabel(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = chartCaptionFor(currentPeriod),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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
    }
}

@Composable
private fun SummaryCards(
    currentPeriod: StatsViewState.StatsPeriod,
    stats: SmokeStats,
    selectedDate: LocalDate,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Total Frequency",
            headline = when (currentPeriod) {
                StatsViewState.StatsPeriod.DAY -> stats.totalDay.toString()
                StatsViewState.StatsPeriod.WEEK -> stats.totalWeek.toString()
                StatsViewState.StatsPeriod.MONTH -> stats.totalMonth.toString()
                StatsViewState.StatsPeriod.YEAR -> stats.yearly.values.sum().toString()
            },
            supporting = "Cigarettes",
            meta = selectedDate.summaryMeta(currentPeriod),
            prominent = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Daily Average",
                headline = String.format(Locale.getDefault(), "%.1f", averageFor(currentPeriod, stats)),
                supporting = averageLabelFor(currentPeriod),
                highlighted = true,
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Peak Window",
                headline = peakBucketFor(currentPeriod, stats),
                supporting = "Highest activity",
            )
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    headline: String,
    supporting: String,
    meta: String? = null,
    prominent: Boolean = false,
    highlighted: Boolean = false,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(if (prominent) 28.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                highlighted -> MaterialTheme.colorScheme.primary
                prominent -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f)
            }
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (prominent) Modifier.height(144.dp) else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (highlighted) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = headline,
                style = if (prominent) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (highlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = if (highlighted) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            meta?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
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
            .fillMaxWidth()
            .aspectRatio(1.55f)
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
            .fillMaxWidth()
            .aspectRatio(1.55f)
            .padding(16.dp)
    )
}

@Composable
fun rememberSmokeAnalyticsVicoTheme(): VicoTheme {
    val primary = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
    val secondary = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.75f)
    val tertiary = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val textColor = MaterialTheme.colorScheme.onBackground
    val errorColor = MaterialTheme.colorScheme.error.copy(alpha = 0.72f)

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

private fun chartCaptionFor(period: StatsViewState.StatsPeriod): String = when (period) {
    StatsViewState.StatsPeriod.DAY -> "Hourly view across the selected day."
    StatsViewState.StatsPeriod.WEEK -> "How smoking volume is distributed across weekdays."
    StatsViewState.StatsPeriod.MONTH -> "Weekly buckets for the selected month."
    StatsViewState.StatsPeriod.YEAR -> "Month-by-month totals for the selected year."
}

private fun LocalDate.analyticsLabel(): String =
    "${month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} $dayOfMonth, $year"

private fun LocalDate.summaryMeta(period: StatsViewState.StatsPeriod): String = when (period) {
    StatsViewState.StatsPeriod.DAY -> "Selected day"
    StatsViewState.StatsPeriod.WEEK -> "Week of ${analyticsLabel()}"
    StatsViewState.StatsPeriod.MONTH -> month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
    StatsViewState.StatsPeriod.YEAR -> "Year to date"
}

private fun averageFor(period: StatsViewState.StatsPeriod, stats: SmokeStats): Float {
    val values = when (period) {
        StatsViewState.StatsPeriod.DAY -> stats.hourly.values
        StatsViewState.StatsPeriod.WEEK -> stats.weekly.values
        StatsViewState.StatsPeriod.MONTH -> stats.monthly.values
        StatsViewState.StatsPeriod.YEAR -> stats.yearly.values
    }
    return values.takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: 0f
}

private fun averageLabelFor(period: StatsViewState.StatsPeriod): String = when (period) {
    StatsViewState.StatsPeriod.DAY -> "Average per hour"
    StatsViewState.StatsPeriod.WEEK -> "Average per weekday"
    StatsViewState.StatsPeriod.MONTH -> "Average per week bucket"
    StatsViewState.StatsPeriod.YEAR -> "Average per month"
}

private fun peakBucketFor(
    period: StatsViewState.StatsPeriod,
    stats: SmokeStats,
): String = when (period) {
    StatsViewState.StatsPeriod.DAY -> stats.hourly.maxByOrNull { it.value }?.key ?: "--"
    StatsViewState.StatsPeriod.WEEK -> stats.weekly.maxByOrNull { it.value }?.key ?: "--"
    StatsViewState.StatsPeriod.MONTH -> stats.monthly.maxByOrNull { it.value }?.key ?: "--"
    StatsViewState.StatsPeriod.YEAR -> stats.yearly.maxByOrNull { it.value }?.key ?: "--"
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


fun StatsViewState.StatsPeriod.toDomainPeriodType(): FetchSmokeStatsUseCase.PeriodType {
    return when (this) {
        StatsViewState.StatsPeriod.DAY -> FetchSmokeStatsUseCase.PeriodType.DAY
        StatsViewState.StatsPeriod.WEEK -> FetchSmokeStatsUseCase.PeriodType.WEEK
        StatsViewState.StatsPeriod.MONTH -> FetchSmokeStatsUseCase.PeriodType.MONTH
        StatsViewState.StatsPeriod.YEAR -> FetchSmokeStatsUseCase.PeriodType.YEAR
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
