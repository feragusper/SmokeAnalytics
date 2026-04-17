package com.feragusper.smokeanalytics.features.home.presentation.mvi.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.home.domain.ElapsedTone
import com.feragusper.smokeanalytics.features.home.domain.FinancialSummary
import com.feragusper.smokeanalytics.features.home.domain.GamificationSummary
import com.feragusper.smokeanalytics.features.home.domain.GapFocusSummary
import com.feragusper.smokeanalytics.features.home.domain.HomeHeroReadout
import com.feragusper.smokeanalytics.features.home.domain.RateSummary
import com.feragusper.smokeanalytics.features.home.domain.gapFocusSummary
import com.feragusper.smokeanalytics.features.home.domain.HomeHeroProgressTone
import com.feragusper.smokeanalytics.features.home.domain.homeHeroProgress
import com.feragusper.smokeanalytics.features.home.domain.homeHeroReadout
import com.feragusper.smokeanalytics.features.home.domain.homeGoalNarrative
import com.feragusper.smokeanalytics.features.home.domain.toElapsedGapLabel
import com.feragusper.smokeanalytics.features.home.domain.toHomeClockLabel
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
    internal val goalProgress: GoalProgress? = null,
    internal val hasActiveGoal: Boolean = false,
    internal val awakeMinutesPerDay: Int = 0,
    internal val dayStartHour: Int = 0,
    internal val bedtimeHour: Int = 0,
    internal val canStartNewDay: Boolean = false,
    internal val elapsedTone: ElapsedTone = ElapsedTone.Urgent,
    internal val error: HomeResult.Error? = null,
) : MVIViewState<HomeIntent> {

    internal val lastSmokeTimeLabel: String?
        get() = lastSmoke?.date?.toHomeClockLabel()

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
                override fun onPreScroll(
                    available: Offset,
                    source: androidx.compose.ui.input.nestedscroll.NestedScrollSource,
                ): Offset = Offset.Zero
            }
        }

        PullToRefreshBox(
            modifier = Modifier.statusBarsPadding(),
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
                timeSinceLastCigarette = timeSinceLastCigarette,
                lastSmokeTimeLabel = lastSmokeTimeLabel,
                greetingTitle = greetingTitle,
                greetingMessage = greetingMessage,
                financialSummary = financialSummary,
                rateSummary = rateSummary,
                goalProgress = goalProgress,
                hasActiveGoal = hasActiveGoal,
                awakeMinutesPerDay = awakeMinutesPerDay,
                dayStartHour = dayStartHour,
                bedtimeHour = bedtimeHour,
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
    timeSinceLastCigarette: Pair<Long, Long>?,
    lastSmokeTimeLabel: String?,
    greetingTitle: String?,
    greetingMessage: String?,
    financialSummary: FinancialSummary?,
    rateSummary: RateSummary?,
    goalProgress: GoalProgress?,
    hasActiveGoal: Boolean,
    awakeMinutesPerDay: Int,
    dayStartHour: Int,
    bedtimeHour: Int,
    canStartNewDay: Boolean,
    elapsedTone: ElapsedTone,
    isLoading: Boolean,
    intent: (HomeIntent) -> Unit,
) {
    val narrative = homeGoalNarrative(
        goalProgress = goalProgress,
        smokesPerDay = smokesPerDay,
        timeSinceLastCigarette = timeSinceLastCigarette,
        awakeMinutesPerDay = awakeMinutesPerDay,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
    )
    val heroProgress = homeHeroProgress(
        goalProgress = goalProgress,
        smokesPerDay = smokesPerDay,
        timeSinceLastCigarette = timeSinceLastCigarette,
        awakeMinutesPerDay = awakeMinutesPerDay,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
    )
    val heroReadout = homeHeroReadout(
        goalProgress = goalProgress,
        smokesPerDay = smokesPerDay,
        timeSinceLastCigarette = timeSinceLastCigarette,
        awakeMinutesPerDay = awakeMinutesPerDay,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
    )
    val elapsedMinutes = timeSinceLastCigarette?.let { it.first * 60 + it.second }
    val gapFocus = gapFocusSummary(
        elapsedMinutes = elapsedMinutes,
        rateSummary = rateSummary,
        goalProgress = goalProgress,
        smokesPerDay = smokesPerDay,
        awakeMinutesPerDay = awakeMinutesPerDay,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HomeHeaderSection(
                greetingTitle = greetingTitle,
                greetingMessage = greetingMessage,
                isLoading = isLoading,
            )
        }
        item {
            GoalHeroSection(
                heroTitle = narrative.heroTitle,
                heroSupporting = narrative.heroSupporting,
                statusLabel = narrative.statusLabel,
                heroProgress = heroProgress,
                heroReadout = heroReadout,
                isLoading = isLoading,
            )
        }
        item {
            LastCigaretteSection(
                lastSmokeTimeLabel = lastSmokeTimeLabel,
                timeSinceLastCigarette = timeSinceLastCigarette,
                gapFocus = gapFocus,
                elapsedTone = elapsedTone,
                isLoading = isLoading,
            )
        }
        item {
            ConsistencySection(
                consistencyLabel = narrative.consistencyLabel,
                statusLabel = narrative.statusLabel,
                isLoading = isLoading,
            )
        }
        item {
            NextActionSection(
                secondaryLabel = if (hasActiveGoal) "Review in You" else "Set in You",
                supporting = narrative.nextActionLabel,
                elapsedTone = elapsedTone,
                isLoading = isLoading,
                onAddSmoke = { intent(HomeIntent.AddSmoke) },
                onOpenGoals = { intent(HomeIntent.OnClickGoals) },
            )
        }
        if (financialSummary != null || smokesPerDay != null) {
            item {
                SupportMetricsSection(
                    smokesPerDay = smokesPerDay,
                    financialSummary = financialSummary,
                    isLoading = isLoading,
                )
            }
        }
        if (canStartNewDay) {
            item {
                EveningResetSection(
                    isLoading = isLoading,
                    onStartNewDay = { intent(HomeIntent.StartNewDay) },
                )
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun HomeHeaderSection(
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
            text = greetingTitle ?: "Home",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = when {
                isLoading -> "Refreshing your goal-first snapshot."
                greetingMessage != null -> greetingMessage
                else -> "Start from the goal, then read the latest gap with as little noise as possible."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GoalHeroSection(
    heroTitle: String,
    heroSupporting: String,
    statusLabel: String,
    heroProgress: com.feragusper.smokeanalytics.features.home.domain.HomeHeroProgress,
    heroReadout: HomeHeroReadout,
    isLoading: Boolean,
) {
    val progress = if (isLoading) 0f else (heroReadout.meterFraction ?: heroProgress.fraction).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Goal",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (isLoading) "Loading today's goal" else heroTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    StatusPill(
                        text = statusLabel,
                        containerColor = heroProgress.tone.pillContainerColor(),
                        contentColor = heroProgress.tone.pillContentColor(),
                    )
                }
            }
            Text(
                text = if (isLoading) "Bringing the latest goal context into focus." else heroSupporting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    heroReadout.meterLabel?.let { label ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                heroReadout.meterValue?.let { value ->
                                    Text(
                                        text = if (isLoading) "--" else value,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = heroProgress.tone.progressColor(),
                                    )
                                }
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(999.dp)),
                                color = heroProgress.tone.progressColor(),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                    heroReadout.metrics.chunked(2).forEach { rowMetrics ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            rowMetrics.forEach { metric ->
                                GoalHeroMetricCard(
                                    label = metric.label,
                                    value = if (isLoading) "--" else metric.value,
                                    supporting = if (isLoading) null else metric.supporting,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowMetrics.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalHeroMetricCard(
    label: String,
    value: String,
    supporting: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            supporting?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LastCigaretteSection(
    lastSmokeTimeLabel: String?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    gapFocus: GapFocusSummary,
    elapsedTone: ElapsedTone,
    isLoading: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Last cigarette",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LastCigaretteValueCard(
                    modifier = Modifier.weight(1f),
                    label = "At",
                    value = if (isLoading) "--:--" else ((lastSmokeTimeLabel?.let { "$it hs" }) ?: "--:--"),
                )
                LastCigaretteValueCard(
                    modifier = Modifier.weight(1f),
                    label = "Time since",
                    value = if (isLoading) "--" else timeSinceLastCigarette.toElapsedGapLabel(),
                    emphasize = true,
                )
            }
            Surface(
                color = elapsedTone.containerColor(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(
                    text = if (isLoading) "Calculating the shape of the current gap." else gapFocus.pulseSummaryText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = elapsedTone.contentColor(),
                )
            }
        }
    }
}

@Composable
private fun LastCigaretteValueCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasize: Boolean = false,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = if (emphasize) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ConsistencySection(
    consistencyLabel: String,
    statusLabel: String,
    isLoading: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Consistency",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (isLoading) "Reading the steadier trend." else consistencyLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (isLoading) "Status pending" else statusLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NextActionSection(
    secondaryLabel: String,
    supporting: String,
    elapsedTone: ElapsedTone,
    isLoading: Boolean,
    onAddSmoke: () -> Unit,
    onOpenGoals: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Next action",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (isLoading) "Deciding the next step." else supporting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onAddSmoke,
                enabled = !isLoading,
                modifier = Modifier
                    .testTag(HomeViewState.TestTags.BUTTON_ADD_SMOKE)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = elapsedTone.buttonContainerColor(),
                    contentColor = elapsedTone.contentColor(),
                ),
            ) {
                Text(
                    text = "Track",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            OutlinedButton(
                onClick = onOpenGoals,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(secondaryLabel)
            }
        }
    }
}

@Composable
private fun SupportMetricsSection(
    smokesPerDay: Int?,
    financialSummary: FinancialSummary?,
    isLoading: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CompactMetricCard(
            modifier = Modifier.weight(1f),
            label = "Today",
            value = if (isLoading) "--" else (smokesPerDay?.toString() ?: "--"),
            supporting = "Cigarettes",
        )
        CompactMetricCard(
            modifier = Modifier.weight(1f),
            label = "Spent",
            value = if (isLoading || financialSummary == null) "--" else {
                financialSummary.spentToday.formatMoney(financialSummary.currencySymbol)
            },
            supporting = "Today",
        )
    }
}

@Composable
private fun CompactMetricCard(
    label: String,
    value: String,
    supporting: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun EveningResetSection(
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
                text = "Starting early?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (isLoading) {
                    "Checking whether you can begin a new day now."
                } else {
                    "If today started earlier than usual, you can reset the reflection window now and keep the home aligned with the day you're actually living."
                },
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
                Text("Start New Day", fontWeight = FontWeight.Bold)
            }
        }
    }
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

@Composable
private fun HomeHeroProgressTone.progressColor(): Color = when (this) {
    HomeHeroProgressTone.Green -> MaterialTheme.colorScheme.primary
    HomeHeroProgressTone.Yellow -> MaterialTheme.colorScheme.tertiary
    HomeHeroProgressTone.Red -> MaterialTheme.colorScheme.error
    HomeHeroProgressTone.Neutral -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
}

@Composable
private fun HomeHeroProgressTone.pillContainerColor(): Color = when (this) {
    HomeHeroProgressTone.Green -> MaterialTheme.colorScheme.primaryContainer
    HomeHeroProgressTone.Yellow -> MaterialTheme.colorScheme.tertiaryContainer
    HomeHeroProgressTone.Red -> MaterialTheme.colorScheme.errorContainer
    HomeHeroProgressTone.Neutral -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun HomeHeroProgressTone.pillContentColor(): Color = when (this) {
    HomeHeroProgressTone.Green -> MaterialTheme.colorScheme.onPrimaryContainer
    HomeHeroProgressTone.Yellow -> MaterialTheme.colorScheme.onTertiaryContainer
    HomeHeroProgressTone.Red -> MaterialTheme.colorScheme.onErrorContainer
    HomeHeroProgressTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
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
            greetingMessage = "You are pacing well for a steadier Tuesday.",
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
