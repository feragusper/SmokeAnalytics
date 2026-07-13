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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
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
import androidx.compose.ui.res.stringResource
import com.feragusper.smokeanalytics.features.stats.presentation.R
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
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStatsPeriod
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.averageSummary
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import com.valentinilk.shimmer.shimmer
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.VicoTheme
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalConfiguration
import java.time.LocalDate as JavaLocalDate
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
        currentPeriod: StatsPeriod = StatsPeriod.WEEK,
        selectedDate: JavaLocalDate = JavaLocalDate.now(),
        onPeriodChange: (StatsPeriod) -> Unit = {},
        onDateChange: (JavaLocalDate) -> Unit = {},
        intent: (StatsIntent) -> Unit,
    ) {
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
                                        text = stringResource(R.string.stats_trends),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = stringResource(R.string.stats_patterns_in_motion),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = when {
                                            displayRefreshLoading -> stringResource(R.string.stats_refreshing_bg)
                                            error != null && stats != null -> stringResource(R.string.stats_latest_refresh_failed)
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
                                        text = if (displayRefreshLoading) stringResource(R.string.stats_refreshing) else selectedDate.summaryMeta(currentPeriod),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                            }
                        } else {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                text = when {
                                    displayRefreshLoading -> stringResource(R.string.stats_refreshing_trends_bg)
                                    error != null && stats != null -> stringResource(R.string.stats_freq_refresh_failed)
                                    else -> selectedDate.summaryMeta(currentPeriod)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (!embedded) {
                            HeaderNavigation(
                                currentPeriod = currentPeriod,
                                selectedDate = selectedDate,
                                onDateChange = onDateChange,
                            )

                            TabRow(
                                modifier = Modifier.padding(top = 4.dp),
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
                                        onClick = { onPeriodChange(period) },
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
                        }
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
                                text = stringResource(R.string.stats_could_not_refresh),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.stats_retry_rebuild),
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
                                    text = stringResource(R.string.stats_smoking_frequency),
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

                    TriggerBreakdownCard(stats = stats)
                }
            }
        }
    }
}

@Composable
private fun TriggerBreakdownCard(stats: SmokeStats) {
    val breakdown = stats.triggerBreakdown
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.stats_by_trigger),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (breakdown.isEmpty()) {
                Text(
                    text = stringResource(R.string.stats_by_trigger_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val total = breakdown.sumOf { it.count }.coerceAtLeast(1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val textMeasurer = rememberTextMeasurer()
                    Canvas(modifier = Modifier.size(140.dp)) {
                        var startAngle = -90f
                        breakdown.forEachIndexed { index, entry ->
                            val sweep = entry.count.toFloat() / total * 360f
                            val sliceColor = pieColors[index % pieColors.size]
                            drawArc(
                                color = sliceColor,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = true,
                            )
                            // Print the share on slices wide enough to fit the label.
                            if (sweep >= 30f) {
                                val midRad = (startAngle + sweep / 2f) * (PI.toFloat() / 180f)
                                val radius = size.minDimension / 2f * 0.62f
                                val pct = (entry.count * 100f / total).roundToInt()
                                val layout = textMeasurer.measure(
                                    text = AnnotatedString("$pct%"),
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sliceColor.luminance() > 0.5f) Color(0xFF10343A) else Color.White,
                                    ),
                                )
                                drawText(
                                    textLayoutResult = layout,
                                    topLeft = Offset(
                                        center.x + radius * cos(midRad) - layout.size.width / 2f,
                                        center.y + radius * sin(midRad) - layout.size.height / 2f,
                                    ),
                                )
                            }
                            startAngle += sweep
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        breakdown.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(pieColors[index % pieColors.size]),
                                )
                                Text(
                                    text = entry.label,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${entry.count} (${entry.count * 100 / total}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Teal ramp shared with the web pie so both platforms read the same.
private val pieColors = listOf(
    Color(0xFF006A6A),
    Color(0xFF1D7B7B),
    Color(0xFF3A8C8C),
    Color(0xFF57A0A0),
    Color(0xFF74B3B3),
    Color(0xFF91C6C6),
    Color(0xFFAED9D9),
    Color(0xFF4A6363),
    Color(0xFF6D8686),
    Color(0xFF9AB0B0),
)

@Composable
private fun SummaryCards(
    currentPeriod: StatsViewState.StatsPeriod,
    stats: SmokeStats,
    selectedDate: JavaLocalDate,
) {
    val averageSummary = averageSummaryFor(currentPeriod, stats, selectedDate)
    val locale = LocalLocale.current.platformLocale
    // Only a single day shows a raw total; for week/month/year a big cumulative number
    // (e.g. "1,000 cigarettes") is discouraging, so lead with the daily average instead.
    val isDay = currentPeriod == StatsViewState.StatsPeriod.DAY
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
                title = if (isDay) stringResource(R.string.stats_total_frequency) else averageSummary.title,
                headline = if (isDay) {
                    stats.totalDay.toString()
                } else {
                    String.format(locale, "%.1f", averageSummary.value)
                },
                supporting = if (isDay) stringResource(R.string.stats_cigarettes) else averageSummary.supporting,
                meta = selectedDate.summaryMeta(currentPeriod),
                prominent = true,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // On a single day, the daily average is its own metric; on longer ranges the
                // average is already the headline above, so this slot only shows the peak.
                if (isDay) {
                    SummaryCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = averageSummary.title,
                        headline = String.format(locale, "%.1f", averageSummary.value),
                        supporting = averageSummary.supporting,
                        highlighted = true,
                        compact = true,
                    )
                }
                SummaryCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.stats_peak_window),
                    headline = peakBucketFor(currentPeriod, stats),
                    supporting = stringResource(R.string.stats_highest_activity),
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
    selectedDate: JavaLocalDate,
    onDateChange: (JavaLocalDate) -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.stats_previous))
        }

        Text(
            text = when (currentPeriod) {
                StatsViewState.StatsPeriod.DAY -> selectedDate.toString()
                StatsViewState.StatsPeriod.WEEK -> "Week of $selectedDate"
                StatsViewState.StatsPeriod.MONTH -> selectedDate.month.getDisplayName(
                    java.time.format.TextStyle.FULL,
                    locale
                )

                StatsViewState.StatsPeriod.YEAR -> selectedDate.year.toString()
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
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
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.stats_next))
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
    val maxVisibleY = max(1.0, accumulatedValues.maxOrNull()?.toDouble() ?: 0.0)

    LaunchedEffect(stats) {
        modelProducer.runTransaction {
            lineSeries {
                series(accumulatedValues.map { it.toFloat() })
            }
        }
    }

    val chartWidth = max(420, stats.size * 48).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    pointSpacing = 48.dp,
                    rangeProvider = remember(maxVisibleY) {
                        CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = maxVisibleY)
                    },
                ),
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

@Composable
private fun chartCaptionFor(period: StatsViewState.StatsPeriod): String = when (period) {
    StatsViewState.StatsPeriod.DAY -> stringResource(R.string.stats_hourly_view)
    StatsViewState.StatsPeriod.WEEK -> stringResource(R.string.stats_week_distribution)
    StatsViewState.StatsPeriod.MONTH -> stringResource(R.string.stats_week_buckets)
    StatsViewState.StatsPeriod.YEAR -> stringResource(R.string.stats_month_totals)
}

private fun JavaLocalDate.analyticsLabel(): String =
    "${month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} $dayOfMonth, $year"

@Composable
private fun JavaLocalDate.summaryMeta(period: StatsViewState.StatsPeriod): String = when (period) {
    StatsViewState.StatsPeriod.DAY -> stringResource(R.string.stats_selected_day)
    StatsViewState.StatsPeriod.WEEK -> stringResource(R.string.stats_week_of, analyticsLabel())
    StatsViewState.StatsPeriod.MONTH -> month.getDisplayName(java.time.format.TextStyle.FULL, LocalConfiguration.current.locales[0])
    StatsViewState.StatsPeriod.YEAR -> stringResource(R.string.stats_year_to_date)
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

private fun averageSummaryFor(
    period: StatsViewState.StatsPeriod,
    stats: SmokeStats,
    selectedDate: JavaLocalDate,
) = stats.averageSummary(
    period = when (period) {
        StatsViewState.StatsPeriod.DAY -> SmokeStatsPeriod.DAY
        StatsViewState.StatsPeriod.WEEK -> SmokeStatsPeriod.WEEK
        StatsViewState.StatsPeriod.MONTH -> SmokeStatsPeriod.MONTH
        StatsViewState.StatsPeriod.YEAR -> SmokeStatsPeriod.YEAR
    },
    selectedYear = selectedDate.year,
    selectedMonth = selectedDate.monthValue,
    selectedDay = selectedDate.dayOfMonth,
)

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
