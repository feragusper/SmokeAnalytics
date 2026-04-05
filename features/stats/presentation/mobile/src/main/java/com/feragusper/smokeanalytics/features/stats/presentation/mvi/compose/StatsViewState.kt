package com.feragusper.smokeanalytics.features.stats.presentation.mvi.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.valentinilk.shimmer.shimmer
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
import kotlin.math.max

/**
 * Represents the state of the Stats screen, encapsulating all UI-related data.
 */
data class StatsViewState(
    val displayLoading: Boolean = false,
    val displayRefreshLoading: Boolean = false,
    val stats: SmokeStats? = null,
    val error: Throwable? = null,
) : MVIViewState<StatsIntent> {

    enum class StatsPeriod { DAY, WEEK, MONTH, YEAR }

    @Composable
    fun Compose(
        refreshNonce: Int = 0,
        embedded: Boolean = false,
        intent: (StatsIntent) -> Unit,
    ) {
        var currentPeriod by remember { mutableStateOf(StatsPeriod.WEEK) }
        var selectedDate by remember { mutableStateOf(LocalDate.now()) }

        LaunchedEffect(refreshNonce, currentPeriod, selectedDate) {
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (embedded) {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
                        }
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (!embedded) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Trends",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "Patterns in motion",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = when {
                                            displayRefreshLoading -> "Refreshing in background"
                                            error != null && stats != null -> "Latest refresh failed"
                                            else -> selectedDate.summaryMeta(currentPeriod)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Card(
                                    shape = RoundedCornerShape(999.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        text = if (displayRefreshLoading) "Refreshing" else selectedDate.summaryMeta(currentPeriod),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                            }
                        } else {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                text = when {
                                    displayRefreshLoading -> "Refreshing trends in background"
                                    error != null && stats != null -> "Latest frequency refresh failed. Showing the last snapshot."
                                    else -> selectedDate.summaryMeta(currentPeriod)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

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
                            }
                        )
                    }
                }

                if (displayLoading && stats == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .shimmer()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                } else if (error != null && stats == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = "Could not refresh trends",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = "Retry to rebuild the frequency view for the selected range.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                stats?.let {
                    SummaryCards(
                        currentPeriod = currentPeriod,
                        stats = stats,
                        selectedDate = selectedDate,
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCard(
                modifier = Modifier.weight(1.6f),
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Daily Average",
                    headline = String.format(Locale.getDefault(), "%.1f", averageFor(currentPeriod, stats)),
                    supporting = averageLabelFor(currentPeriod),
                    highlighted = true,
                    compact = true,
                )
                SummaryCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Peak Window",
                    headline = peakBucketFor(currentPeriod, stats),
                    supporting = "Highest activity",
                    compact = true,
                )
            }
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
    compact: Boolean = false,
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
                .then(
                    when {
                        prominent -> Modifier.height(176.dp)
                        compact -> Modifier.height(132.dp)
                        else -> Modifier
                    }
                )
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
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
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

    val chartWidth = max(560, stats.size * 72).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
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
                .width(chartWidth)
                .height(260.dp)
        )
    }
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

    val chartWidth = max(560, stats.size * 72).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
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
                .width(chartWidth)
                .height(260.dp)
        )
    }
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
