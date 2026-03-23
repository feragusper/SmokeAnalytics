package com.feragusper.smokeanalytics.features.home.presentation.mvi.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.home.domain.ElapsedTone
import com.feragusper.smokeanalytics.features.home.domain.FinancialSummary
import com.feragusper.smokeanalytics.features.home.domain.GamificationSummary
import com.feragusper.smokeanalytics.features.home.domain.RateSummary
import com.feragusper.smokeanalytics.features.home.presentation.R
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.preferences.domain.formatMoney
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.valentinilk.shimmer.shimmer

data class HomeViewState(
    internal val displayLoading: Boolean = false,
    internal val displayRefreshLoading: Boolean = false,
    internal val smokesPerDay: Int? = null,
    internal val smokesPerWeek: Int? = null,
    internal val smokesPerMonth: Int? = null,
    internal val timeSinceLastCigarette: Pair<Long, Long>? = null,
    internal val latestSmokes: List<Smoke>? = null,
    internal val lastSmoke: Smoke? = null,
    internal val greetingTitle: String? = null,
    internal val greetingMessage: String? = null,
    internal val financialSummary: FinancialSummary? = null,
    internal val rateSummary: RateSummary? = null,
    internal val gamificationSummary: GamificationSummary? = null,
    internal val canStartNewDay: Boolean = false,
    internal val elapsedTone: ElapsedTone = ElapsedTone.Urgent,
    internal val error: HomeResult.Error? = null,
) : MVIViewState<HomeIntent> {

    interface TestTags {
        companion object {
            const val BUTTON_ADD_SMOKE = "buttonAddSmoke"
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Compose(
        onFabConfigChanged: (Boolean, ElapsedTone, (() -> Unit)?) -> Unit,
        intent: (HomeIntent) -> Unit,
    ) {
        val pullToRefreshState = remember {
            object : PullToRefreshState {
                private val anim = Animatable(0f, Float.VectorConverter)
                override val distanceFraction get() = anim.value
                override val isAnimating: Boolean get() = anim.isRunning

                override suspend fun animateToThreshold() {
                    anim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                }

                override suspend fun animateToHidden() {
                    anim.animateTo(0f)
                }

                override suspend fun snapTo(targetValue: Float) {
                    anim.snapTo(targetValue)
                }
            }
        }

        LaunchedEffect(displayLoading, elapsedTone) {
            onFabConfigChanged(false, elapsedTone, null)
        }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): Offset =
                    Offset.Zero
            }
        }

        PullToRefreshBox(
            isRefreshing = displayRefreshLoading,
            onRefresh = { intent(HomeIntent.FetchSmokes) },
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = displayRefreshLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            },
        ) {
            HomeContent(
                nestedScrollConnection = nestedScrollConnection,
                smokesPerDay = smokesPerDay,
                smokesPerWeek = smokesPerWeek,
                smokesPerMonth = smokesPerMonth,
                timeSinceLastCigarette = timeSinceLastCigarette,
                greetingTitle = greetingTitle,
                greetingMessage = greetingMessage,
                financialSummary = financialSummary,
                rateSummary = rateSummary,
                gamificationSummary = gamificationSummary,
                canStartNewDay = canStartNewDay,
                elapsedTone = elapsedTone,
                isLoading = displayLoading,
                intent = intent,
            )
        }
    }
}

@Composable
private fun HomeContent(
    nestedScrollConnection: NestedScrollConnection,
    smokesPerDay: Int?,
    smokesPerWeek: Int?,
    smokesPerMonth: Int?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    greetingTitle: String?,
    greetingMessage: String?,
    financialSummary: FinancialSummary?,
    rateSummary: RateSummary?,
    gamificationSummary: GamificationSummary?,
    canStartNewDay: Boolean,
    elapsedTone: ElapsedTone,
    isLoading: Boolean,
    intent: (HomeIntent) -> Unit,
) {
    val elapsedMinutes = timeSinceLastCigarette?.let { it.first * 60 + it.second }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PulseHeaderSection(
                greetingTitle = greetingTitle,
                greetingMessage = greetingMessage,
                isLoading = isLoading,
            )
        }
        item {
            PulseHeroSection(
                timeSinceLastCigarette = timeSinceLastCigarette,
                elapsedTone = elapsedTone,
                rateSummary = rateSummary,
                isLoading = isLoading,
            )
        }
        item {
            AddSmokeSection(
                elapsedTone = elapsedTone,
                isLoading = isLoading,
                onAddSmoke = { intent(HomeIntent.AddSmoke) },
            )
        }
        item {
            PrimaryMetricGrid(
                smokesPerDay = smokesPerDay,
                gamificationSummary = gamificationSummary,
                isLoading = isLoading,
                onHistoryClick = { intent(HomeIntent.OnClickHistory) },
            )
        }
        item {
            RecoveryStatusSection(
                elapsedTone = elapsedTone,
                timeSinceLastCigaretteMinutes = elapsedMinutes,
                gamificationSummary = gamificationSummary,
                isLoading = isLoading,
            )
        }
        item {
            FinancialInsightSection(
                financialSummary = financialSummary,
                rateSummary = rateSummary,
                isLoading = isLoading,
            )
        }
        item {
            ArchiveSnapshotSection(
                smokesPerWeek = smokesPerWeek,
                smokesPerMonth = smokesPerMonth,
                rateSummary = rateSummary,
                isLoading = isLoading,
                onHistoryClick = { intent(HomeIntent.OnClickHistory) },
            )
        }
        if (canStartNewDay) {
            item {
                EveningResetSection(
                    greetingMessage = greetingMessage,
                    isLoading = isLoading,
                    onStartNewDay = { intent(HomeIntent.StartNewDay) },
                )
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun PulseHeaderSection(
    greetingTitle: String?,
    greetingMessage: String?,
    isLoading: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = greetingTitle ?: "The Pulse",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = when {
                isLoading -> "Refreshing your daily snapshot."
                greetingMessage != null -> greetingMessage
                else -> "A quieter way to read your smoking rhythm, money, and recovery cues."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PulseHeroSection(
    timeSinceLastCigarette: Pair<Long, Long>?,
    elapsedTone: ElapsedTone,
    rateSummary: RateSummary?,
    isLoading: Boolean,
) {
    val elapsedMinutes = timeSinceLastCigarette?.let { it.first * 60 + it.second }
    val averageGap = rateSummary?.averageIntervalMinutesToday
    val progress = when {
        isLoading -> 0f
        elapsedMinutes == null || averageGap == null || averageGap <= 0 -> 0.22f
        else -> (elapsedMinutes.toFloat() / (averageGap * 1.5f)).coerceIn(0.08f, 1f)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(220.dp),
                strokeWidth = 12.dp,
                color = elapsedTone.buttonContainerColor(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Mindful Gap",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .size(112.dp, 40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmer()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    )
                } else {
                    Text(
                        text = timeSinceLastCigarette.toPulseValue(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "Minutes ago",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Surface(
            color = elapsedTone.containerColor(),
            shape = RoundedCornerShape(999.dp),
        ) {
            Text(
                text = pulseSummaryText(
                    elapsedMinutes = elapsedMinutes,
                    averageGapMinutes = averageGap,
                ),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = elapsedTone.contentColor(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AddSmokeSection(
    elapsedTone: ElapsedTone,
    isLoading: Boolean,
    onAddSmoke: () -> Unit,
) {
    Button(
        onClick = onAddSmoke,
        enabled = !isLoading,
        modifier = Modifier
            .testTag(HomeViewState.TestTags.BUTTON_ADD_SMOKE)
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = elapsedTone.buttonContainerColor(),
            contentColor = elapsedTone.contentColor(),
        ),
    ) {
        Text(
            text = stringResource(R.string.home_button_track),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PrimaryMetricGrid(
    smokesPerDay: Int?,
    gamificationSummary: GamificationSummary?,
    isLoading: Boolean,
    onHistoryClick: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        HighlightMetricCard(
            modifier = Modifier.weight(1f),
            eyebrow = "Today's Count",
            value = smokesPerDay?.toString(),
            supporting = "Cigarettes",
            isLoading = isLoading,
            onClick = onHistoryClick,
        )
        HighlightMetricCard(
            modifier = Modifier.weight(1f),
            eyebrow = "Recovery Points",
            value = gamificationSummary?.points?.toString(),
            supporting = gamificationSummary?.let { "Next ${it.nextMilestoneHours}h" } ?: "Momentum",
            isLoading = isLoading,
        )
    }
}

@Composable
private fun HighlightMetricCard(
    eyebrow: String,
    value: String?,
    supporting: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .aspectRatio(1.02f)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .size(96.dp, 34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmer()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                )
            } else {
                Text(
                    text = value ?: "--",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecoveryStatusSection(
    elapsedTone: ElapsedTone,
    timeSinceLastCigaretteMinutes: Long?,
    gamificationSummary: GamificationSummary?,
    isLoading: Boolean,
) {
    val targetMinutes = (gamificationSummary?.nextMilestoneHours ?: 1).coerceAtLeast(1) * 60
    val progress = if (timeSinceLastCigaretteMinutes == null) 0f
    else (timeSinceLastCigaretteMinutes.toFloat() / targetMinutes.toFloat()).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Recovery Status",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = elapsedTone.recoveryTitle(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = if (isLoading) "--" else "${(progress * 100).toInt()}% to next reset",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = elapsedTone.buttonContainerColor(),
                trackColor = MaterialTheme.colorScheme.surface,
            )

            Text(
                text = when {
                    isLoading -> "Calculating your next recovery milestone."
                    gamificationSummary != null -> "You are working toward the next ${gamificationSummary.nextMilestoneHours}h milestone."
                    else -> "Each longer gap compounds into steadier recovery."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FinancialInsightSection(
    financialSummary: FinancialSummary?,
    rateSummary: RateSummary?,
    isLoading: Boolean,
) {
    val monthTarget = financialSummary?.let { (it.spentWeek * 4).coerceAtLeast(it.spentToday) }
    val budgetProgress = if (financialSummary == null || monthTarget == null || monthTarget <= 0.0) 0f
    else (financialSummary.spentToday / monthTarget).toFloat().coerceIn(0f, 1f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Financial Insight",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Budget Mindfulness",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmer()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    MoneyMetric(
                        label = "Spent today",
                        value = financialSummary?.spentToday?.formatMoney(financialSummary.currencySymbol) ?: "--",
                    )
                    MoneyMetric(
                        label = "Week",
                        value = financialSummary?.spentWeek?.formatMoney(financialSummary.currencySymbol) ?: "--",
                    )
                }
                LinearProgressIndicator(
                    progress = { budgetProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = rateSummary?.latestIntervalMinutes?.let { "Latest interval ${it.toGapLabel()}" }
                        ?: "Track daily spend against the pace you are actually living.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MoneyMetric(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ArchiveSnapshotSection(
    smokesPerWeek: Int?,
    smokesPerMonth: Int?,
    rateSummary: RateSummary?,
    isLoading: Boolean,
    onHistoryClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHistoryClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Archive Snapshot",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SnapshotMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Week",
                    value = smokesPerWeek?.toString(),
                    supporting = rateSummary?.let { "%.1f / day".format(it.averageSmokesPerDayWeek) },
                    isLoading = isLoading,
                )
                SnapshotMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Month",
                    value = smokesPerMonth?.toString(),
                    supporting = rateSummary?.let { "%.1f / day".format(it.averageSmokesPerDayMonth) },
                    isLoading = isLoading,
                )
            }
        }
    }
}

@Composable
private fun SnapshotMetricCard(
    title: String,
    value: String?,
    supporting: String?,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .size(72.dp, 26.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmer()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                )
            } else {
                Text(
                    text = value ?: "--",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = supporting ?: "Open History for full detail",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EveningResetSection(
    greetingMessage: String?,
    isLoading: Boolean,
    onStartNewDay: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.28f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Ready to Reset?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (isLoading) "Checking whether you can start a new day."
                else greetingMessage ?: "Your day boundary is close. Reset the day when you want to start the next reflection window cleanly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onStartNewDay,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
            ) {
                Text(text = "Start New Day", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun Pair<Long, Long>?.toPulseValue(): String = this?.let { (hours, minutes) ->
    val totalMinutes = hours * 60 + minutes
    "%02d:%02d".format(totalMinutes / 60, totalMinutes % 60)
} ?: "--:--"

private fun pulseSummaryText(
    elapsedMinutes: Long?,
    averageGapMinutes: Int?,
): String = when {
    elapsedMinutes == null -> "Log a smoke or refresh to rebuild today's pulse."
    averageGapMinutes == null || averageGapMinutes <= 0 -> "Stay with this gap and watch the daily pulse settle."
    elapsedMinutes >= averageGapMinutes -> {
        val delta = elapsedMinutes - averageGapMinutes
        "You are ${delta} minutes beyond your average gap today."
    }
    else -> {
        val remaining = averageGapMinutes - elapsedMinutes
        "$remaining minutes until you meet today's average gap."
    }
}

private fun Int.toGapLabel(): String = when {
    this >= 60 -> "${this / 60}h ${this % 60}m"
    else -> "${this}m"
}

private fun ElapsedTone.recoveryTitle(): String = when (this) {
    ElapsedTone.Urgent -> "Level 1 Reset"
    ElapsedTone.Warning -> "Level 2 Recovery"
    ElapsedTone.Caution -> "Level 3 Recovery"
    ElapsedTone.Calm -> "Level 4 Vitality"
}

@Composable
private fun ElapsedTone.containerColor(): Color = when (this) {
    ElapsedTone.Urgent -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.68f)
    ElapsedTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.58f)
    ElapsedTone.Caution -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.46f)
    ElapsedTone.Calm -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
}

@Composable
private fun ElapsedTone.contentColor(): Color = when (this) {
    ElapsedTone.Urgent -> MaterialTheme.colorScheme.onErrorContainer
    ElapsedTone.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
    ElapsedTone.Caution -> MaterialTheme.colorScheme.onSecondaryContainer
    ElapsedTone.Calm -> MaterialTheme.colorScheme.onPrimaryContainer
}

@Composable
private fun ElapsedTone.buttonContainerColor(): Color = when (this) {
    ElapsedTone.Urgent -> MaterialTheme.colorScheme.errorContainer
    ElapsedTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer
    ElapsedTone.Caution -> MaterialTheme.colorScheme.secondaryContainer
    ElapsedTone.Calm -> MaterialTheme.colorScheme.primaryContainer
}

@CombinedPreviews
@Composable
private fun HomeViewLoadingPreview() {
    SmokeAnalyticsTheme {
        HomeViewState(displayLoading = true).Compose({ _, _, _ -> }, {})
    }
}

@CombinedPreviews
@Composable
private fun HomeViewPreview() {
    SmokeAnalyticsTheme {
        HomeViewState(
            smokesPerDay = 3,
            smokesPerWeek = 16,
            smokesPerMonth = 58,
            timeSinceLastCigarette = 4L to 22L,
            greetingTitle = "Good morning",
            greetingMessage = "You are 12 minutes beyond your average gap today.",
            financialSummary = FinancialSummary(
                spentToday = 2.45,
                spentWeek = 13.8,
                spentMonth = 40.0,
                currencySymbol = "$",
            ),
            rateSummary = RateSummary(
                latestIntervalMinutes = 262,
                averageIntervalMinutesToday = 250,
                averageSmokesPerDayWeek = 2.4,
                averageSmokesPerDayMonth = 2.8,
            ),
            gamificationSummary = GamificationSummary(
                currentStreakHours = 4,
                longestStreakHours = 12,
                points = 842,
                nextMilestoneHours = 6,
                badges = listOf("2h", "4h"),
            ),
            canStartNewDay = true,
            elapsedTone = ElapsedTone.Calm,
        ).Compose({ _, _, _ -> }, {})
    }
}
