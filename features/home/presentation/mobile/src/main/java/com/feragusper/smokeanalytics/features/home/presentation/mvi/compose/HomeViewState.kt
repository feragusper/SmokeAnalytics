package com.feragusper.smokeanalytics.features.home.presentation.mvi.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.home.domain.ElapsedTone
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
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationTrackingAvailability
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingStats
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.TriggerOption
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay
import kotlin.time.Clock

/**
 * The celebration shown after a craving wait is resolved.
 */
data class CravingCelebration(
    val outcome: CravingOutcome,
    val points: Int,
)

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
    internal val quitReason: String = "",
    internal val use24HourClock: Boolean = true,
    internal val rateSummary: RateSummary? = null,
    internal val gamificationSummary: GamificationSummary? = null,
    internal val goalProgress: GoalProgress? = null,
    internal val hasActiveGoal: Boolean = false,
    internal val awakeMinutesPerDay: Int = 0,
    internal val dayStartHour: Int = 0,
    internal val bedtimeHour: Int = 0,
    internal val canStartNewDay: Boolean = false,
    internal val elapsedTone: ElapsedTone = ElapsedTone.Urgent,
    internal val locationTrackingAvailability: LocationTrackingAvailability = LocationTrackingAvailability(
        preferenceEnabled = false,
        permissionGranted = false,
        providerEnabled = false,
    ),
    internal val error: HomeResult.Error? = null,
    internal val monthTrend: Int? = null,
    internal val monthTrendDelta: Int? = null,
    internal val activeCraving: Craving? = null,
    internal val cravingStats: CravingStats? = null,
    internal val showCravingHint: Boolean = false,
    internal val cravingCelebration: CravingCelebration? = null,
    internal val pendingRelationshipSmokes: List<Smoke> = emptyList(),
    internal val relationshipPromptSmokeId: String? = null,
    /** Null until the first fetch resolves — the prompt shows a loading state meanwhile. */
    internal val availableTriggers: List<TriggerOption>? = null,
) : MVIViewState<HomeIntent> {

    internal val lastSmokeTimeLabel: String?
        get() = lastSmoke?.date?.toHomeClockLabel(use24HourClock = use24HourClock)

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
            onFabConfigChanged(!displayLoading, elapsedTone) {
                intent(HomeIntent.AddSmoke)
            }
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
                quitReason = quitReason,
                rateSummary = rateSummary,
                goalProgress = goalProgress,
                hasActiveGoal = hasActiveGoal,
                awakeMinutesPerDay = awakeMinutesPerDay,
                dayStartHour = dayStartHour,
                bedtimeHour = bedtimeHour,
                canStartNewDay = canStartNewDay,
                elapsedTone = elapsedTone,
                locationTrackingAvailability = locationTrackingAvailability,
                isLoading = displayLoading,
                error = error,
                monthTrend = monthTrend,
                monthTrendDelta = monthTrendDelta,
                activeCraving = activeCraving,
                cravingStats = cravingStats,
                showCravingHint = showCravingHint,
                pendingRelationshipSmokes = pendingRelationshipSmokes.map {
                    PendingTriggerSmoke(id = it.id, label = it.date.toPendingTriggerLabel())
                },
                onOpenRelationship = { id -> intent(HomeIntent.OpenRelationshipPrompt(id)) },
                intent = intent,
            )
        }

        cravingCelebration?.let { celebration ->
            CravingCelebrationDialog(
                celebration = celebration,
                onDismiss = { intent(HomeIntent.DismissCravingCelebration) },
            )
        }

        val promptSmokeId = relationshipPromptSmokeId
        if (promptSmokeId != null) {
            val dateLabel = pendingRelationshipSmokes
                .firstOrNull { it.id == promptSmokeId }
                ?.date
                ?.toPendingTriggerLabel()
            RelationshipPromptSheet(
                availableTriggers = availableTriggers,
                dateLabel = dateLabel,
                onSave = { tags -> intent(HomeIntent.SaveSmokeRelationship(promptSmokeId, tags)) },
                onSkip = { intent(HomeIntent.SkipSmokeRelationship(promptSmokeId)) },
                onDismiss = { intent(HomeIntent.DismissRelationshipPrompt) },
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
    quitReason: String,
    rateSummary: RateSummary?,
    goalProgress: GoalProgress?,
    hasActiveGoal: Boolean,
    awakeMinutesPerDay: Int,
    dayStartHour: Int,
    bedtimeHour: Int,
    canStartNewDay: Boolean,
    elapsedTone: ElapsedTone,
    locationTrackingAvailability: LocationTrackingAvailability,
    isLoading: Boolean,
    error: HomeResult.Error?,
    monthTrend: Int?,
    monthTrendDelta: Int?,
    activeCraving: Craving?,
    cravingStats: CravingStats?,
    showCravingHint: Boolean,
    pendingRelationshipSmokes: List<PendingTriggerSmoke>,
    onOpenRelationship: (String) -> Unit,
    intent: (HomeIntent) -> Unit,
) {
    val hasLoadedContent = smokesPerDay != null || timeSinceLastCigarette != null || goalProgress != null
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
                locationTrackingAvailability = locationTrackingAvailability,
                isLoading = isLoading,
            )
        }
        if (error != null) {
            item {
                HomeErrorSection(
                    error = error,
                    hasLoadedContent = hasLoadedContent,
                    onRetry = { intent(HomeIntent.FetchSmokes) },
                )
            }
        }
        if (!hasLoadedContent && error != null) {
            item { Spacer(modifier = Modifier.height(24.dp)) }
            return@LazyColumn
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
        if (!isLoading && hasLoadedContent) {
            if (pendingRelationshipSmokes.isNotEmpty()) {
                item {
                    RelationshipReminderCard(
                        pending = pendingRelationshipSmokes,
                        onOpen = onOpenRelationship,
                    )
                }
            }
            if (showCravingHint) {
                item { CravingHintBanner(onDismiss = { intent(HomeIntent.DismissCravingHint) }) }
            }
            item {
                if (activeCraving != null) {
                    CravingCountdownCard(
                        quitReason = quitReason,
                        craving = activeCraving,
                        onResolve = { smoked ->
                            intent(HomeIntent.ResolveCraving(craving = activeCraving, smoked = smoked))
                        },
                    )
                } else {
                    CravingPromptCard(quitReason = quitReason, onTrack = { intent(HomeIntent.TrackCraving) })
                }
            }
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
        if (canStartNewDay) {
            item {
                EveningResetSection(
                    isLoading = isLoading,
                    onStartNewDay = { intent(HomeIntent.StartNewDay) },
                )
            }
        }
        cravingStats?.takeIf { it.total > 0 }?.let { stats ->
            item { CravingStatsCard(stats = stats) }
        }
        monthTrend?.let { trend ->
            item {
                HomeTrendCard(trendValue = trend, delta = monthTrendDelta)
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun HomeTrendCard(trendValue: Int, delta: Int?) {
    // trendValue is the reduction vs last month: positive = smoking less (good),
    // negative = smoking more (bad). Colour and copy follow that, not a fixed green.
    val improving = trendValue > 0
    val worsening = trendValue < 0
    val containerColor = when {
        improving -> MaterialTheme.colorScheme.primary
        worsening -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        improving -> MaterialTheme.colorScheme.onPrimary
        worsening -> MaterialTheme.colorScheme.onError
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val headline = when {
        improving -> "Smoking less than last month"
        worsening -> "Smoking more than last month"
        else -> "Same pace as last month"
    }
    // delta = current - previous (same elapsed window). Negative = fewer so far.
    val deltaLabel = delta?.let {
        when {
            it < 0 -> "${-it} fewer ${pluralCigarettes(-it)} so far"
            it > 0 -> "$it more ${pluralCigarettes(it)} so far"
            else -> "Same count so far"
        }
    }

    androidx.compose.material3.Card(
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vs last month",
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor.copy(alpha = 0.72f),
                )
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor,
                )
                deltaLabel?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.82f),
                    )
                }
            }
            Surface(
                color = contentColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (improving) "↘" else if (worsening) "↗" else "→",
                        style = MaterialTheme.typography.titleLarge,
                        color = contentColor,
                    )
                    Text(
                        text = "${if (trendValue > 0) "+" else ""}$trendValue%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                    )
                }
            }
        }
    }
}

private fun pluralCigarettes(count: Int): String = if (count == 1) "cigarette" else "cigarettes"

/** Personal reminder shown during a craving, only when the user set a reason. */
@Composable
private fun QuitReasonReminder(quitReason: String) {
    if (quitReason.isBlank()) return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = "Remember why: $quitReason",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun CravingPromptCard(quitReason: String, onTrack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
        border = sectionCardBorder(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Feeling the urge?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Track the craving before lighting up. If it isn't time yet, we'll help you wait it out and reward the win.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            QuitReasonReminder(quitReason)
            Button(
                onClick = onTrack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("I feel like smoking", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CravingHintBanner(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(20.dp),
        border = innerCardBorder(),
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, top = 6.dp, bottom = 6.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "It's already a good time — go ahead when you want.",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun CravingCountdownCard(
    quitReason: String,
    craving: Craving,
    onResolve: (smoked: Boolean) -> Unit,
) {
    val target = craving.targetAt
    var remainingSeconds by remember(craving.id) {
        mutableStateOf(target?.secondsFromNow() ?: 0L)
    }
    LaunchedEffect(craving.id) {
        if (target != null) {
            while (true) {
                remainingSeconds = target.secondsFromNow()
                if (remainingSeconds <= 0L) break
                delay(1_000)
            }
        }
    }
    val done = remainingSeconds <= 0L

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 1.dp,
        border = sectionCardBorder(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (done) "You made it! 🎉" else "Hold on 💪",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Highlighted inner panel: countdown while waiting, message when done.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (done) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                },
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (!done) {
                        Text(
                            text = remainingSeconds.toCountdownLabel(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                    Text(
                        text = if (done) {
                            "The wait is over. Smoke it now if you still want it, or let it go for the full reward."
                        } else {
                            "Until your next cigarette fits the goal. You've got this."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (done) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        },
                        textAlign = TextAlign.Center,
                    )
                }
            }

            QuitReasonReminder(quitReason)

            // onResolve(true)  -> the user smoked (gave in while waiting / postponed once done)
            // onResolve(false) -> the urge passed without smoking (resisted)
            if (done) {
                // The wait paid off. Now smoking fits the goal: log it (postponed) or let it go (resisted).
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { onResolve(false) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text("I'm good")
                    }
                    Button(
                        onClick = { onResolve(true) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text("Log the cigarette", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // While waiting the only manual action is the give-in escape hatch.
                // Resisting is automatic: the card flips to "You made it!" when the countdown ends.
                OutlinedButton(
                    onClick = { onResolve(true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("I smoked anyway")
                }
            }
        }
    }
}

@Composable
private fun CravingStatsCard(stats: CravingStats) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        border = sectionCardBorder(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Cravings",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CravingStatCell(
                    modifier = Modifier.weight(1f),
                    value = "${stats.resisted}",
                    label = "Resisted",
                )
                CravingStatCell(
                    modifier = Modifier.weight(1f),
                    value = "${stats.postponed}",
                    label = "Postponed",
                )
                CravingStatCell(
                    modifier = Modifier.weight(1f),
                    value = stats.minutesWaited.toWaitedLabel(),
                    label = "Waited",
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Reward points",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${stats.points}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CravingStatCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(18.dp),
        border = innerCardBorder(),
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = 70.dp)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CravingCelebrationDialog(
    celebration: CravingCelebration,
    onDismiss: () -> Unit,
) {
    val resisted = celebration.outcome == CravingOutcome.RESISTED
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text = if (resisted) "Urge beaten!" else "Nice and slow",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = if (resisted) {
                    "You let the craving pass without smoking. +${celebration.points} points earned."
                } else {
                    "You waited it out before smoking. +${celebration.points} points earned."
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Nice")
            }
        },
    )
}

private fun kotlin.time.Instant.secondsFromNow(): Long =
    (this - Clock.System.now()).inWholeSeconds

private fun Long.toCountdownLabel(): String {
    val total = coerceAtLeast(0)
    val minutes = total / 60
    val seconds = total % 60
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        "${hours}:${mins.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${mins.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}

private fun Long.toWaitedLabel(): String {
    val minutes = coerceAtLeast(0)
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours <= 0 -> "${mins}m"
        mins == 0L -> "${hours}h"
        else -> "${hours}h ${mins}m"
    }
}

@Composable
private fun HomeErrorSection(
    error: HomeResult.Error,
    hasLoadedContent: Boolean,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = if (error == HomeResult.Error.NotLoggedIn) "Session required" else "Could not complete action",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = when {
                    error == HomeResult.Error.NotLoggedIn -> "Sign in again to keep Home, goals, and the latest gap synced."
                    error is HomeResult.Error.Generic && error.debugMessage != null -> error.debugMessage
                    hasLoadedContent -> "Keeping the last visible state. Retry when the connection or quota recovers."
                    else -> "Home could not load your smoke data. Retry when the connection or quota recovers."
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onErrorContainer,
                    contentColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun HomeHeaderSection(
    greetingTitle: String?,
    greetingMessage: String?,
    locationTrackingAvailability: LocationTrackingAvailability,
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
        LocationTrackingChip(locationTrackingAvailability = locationTrackingAvailability)
    }
}

@Composable
private fun LocationTrackingChip(
    locationTrackingAvailability: LocationTrackingAvailability,
) {
    val isReady = locationTrackingAvailability.isReady
    val label = when {
        isReady -> "Location On"
        !locationTrackingAvailability.preferenceEnabled -> "Location Off"
        !locationTrackingAvailability.permissionGranted -> "Location Off - permission"
        !locationTrackingAvailability.providerEnabled -> "Location Off - system"
        else -> "Location Off"
    }
    val containerColor = if (isReady) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.52f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (isReady) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(999.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        border = sectionCardBorder(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (isLoading) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.16f)
                        .height(14.dp),
                    shape = RoundedCornerShape(8.dp),
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.66f)
                            .height(34.dp),
                        shape = RoundedCornerShape(14.dp),
                    )
                    SkeletonBlock(
                        modifier = Modifier
                            .width(120.dp)
                            .height(34.dp),
                        shape = RoundedCornerShape(999.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(14.dp),
                        shape = RoundedCornerShape(8.dp),
                    )
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.74f)
                            .height(14.dp),
                        shape = RoundedCornerShape(8.dp),
                    )
                }
            } else {
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
                            text = heroTitle,
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
                    text = heroSupporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 1.dp,
                border = innerCardBorder(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isLoading) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SkeletonBlock(
                                    modifier = Modifier
                                        .fillMaxWidth(0.34f)
                                        .height(12.dp),
                                    shape = RoundedCornerShape(8.dp),
                                )
                                SkeletonBlock(
                                    modifier = Modifier
                                        .width(56.dp)
                                        .height(20.dp),
                                    shape = RoundedCornerShape(10.dp),
                                )
                            }
                            SkeletonBlock(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                shape = RoundedCornerShape(999.dp),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            repeat(2) {
                                GoalHeroMetricSkeleton(modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
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
                                            text = value,
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
                        heroReadout.metrics.take(4).chunked(2).forEach { rowMetrics ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rowMetrics.forEach { metric ->
                                    GoalHeroMetricCard(
                                        label = metric.label,
                                        value = metric.value,
                                        supporting = metric.supporting,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
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
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        border = innerCardBorder(),
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = 86.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            supporting?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun GoalHeroMetricSkeleton(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        border = innerCardBorder(),
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = 86.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.42f)
                        .height(10.dp),
                    shape = RoundedCornerShape(8.dp),
                )
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .height(22.dp),
                    shape = RoundedCornerShape(12.dp),
                )
            }
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .height(12.dp),
                shape = RoundedCornerShape(8.dp),
            )
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
        border = sectionCardBorder(),
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
                    value = if (isLoading) "" else ((lastSmokeTimeLabel?.let { "$it hs" }) ?: "--:--"),
                    loading = isLoading,
                )
                LastCigaretteValueCard(
                    modifier = Modifier.weight(1f),
                    label = "Time since",
                    value = if (isLoading) "" else timeSinceLastCigarette.toElapsedGapLabel(),
                    loading = isLoading,
                )
            }
            Surface(
                color = elapsedTone.containerColor(),
                shape = RoundedCornerShape(20.dp),
            ) {
                if (isLoading) {
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(20.dp),
                        shape = RoundedCornerShape(10.dp),
                    )
                } else {
                    Text(
                        text = gapFocus.pulseSummaryText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = elapsedTone.contentColor(),
                    )
                }
            }
        }
    }
}

@Composable
private fun LastCigaretteValueCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 1.dp,
        border = innerCardBorder(),
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = 74.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (loading) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.58f)
                        .height(24.dp),
                    shape = RoundedCornerShape(12.dp),
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ConsistencySection(
    consistencyLabel: String,
    statusLabel: String,
    isLoading: Boolean,
) {
    val streakDays = consistencyLabel.completedStreakDays()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        border = sectionCardBorder(),
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
            if (isLoading) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(24.dp),
                    shape = RoundedCornerShape(12.dp),
                )
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.34f)
                        .height(12.dp),
                    shape = RoundedCornerShape(8.dp),
                )
            } else {
                Text(
                    text = consistencyLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                ConsistencyMilestoneRow(streakDays = streakDays)
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ConsistencyMilestoneRow(
    streakDays: Int?,
) {
    val milestones = listOf(
        ConsistencyMilestone(days = 7, icon = Icons.Filled.CheckCircle),
        ConsistencyMilestone(days = 14, icon = Icons.Filled.LocalFireDepartment),
        ConsistencyMilestone(days = 30, icon = Icons.Filled.Star),
        ConsistencyMilestone(days = 60, icon = Icons.Filled.EmojiEvents),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        milestones.forEach { milestone ->
            val completed = streakDays != null && streakDays >= milestone.days
            Surface(
                color = if (completed) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                contentColor = if (completed) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                shape = RoundedCornerShape(999.dp),
                border = innerCardBorder(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = milestone.icon,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = "${milestone.days}d",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private data class ConsistencyMilestone(
    val days: Int,
    val icon: ImageVector,
)

private fun String.completedStreakDays(): Int? =
    Regex("""(\d+)\s+days?\s+completed\s+in\s+a\s+row""")
        .find(this)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()

@Composable
private fun SkeletonBlock(
    modifier: Modifier,
    shape: RoundedCornerShape,
) {
    Spacer(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f))
            .shimmer()
    )
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
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        border = sectionCardBorder(),
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
private fun sectionCardBorder(): BorderStroke =
    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))

@Composable
private fun innerCardBorder(): BorderStroke =
    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))

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
