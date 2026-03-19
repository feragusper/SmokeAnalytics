package com.feragusper.smokeanalytics.features.home.presentation.mvi.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.home.presentation.R
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.domain.ElapsedTone
import com.feragusper.smokeanalytics.features.home.domain.FinancialSummary
import com.feragusper.smokeanalytics.features.home.domain.GamificationSummary
import com.feragusper.smokeanalytics.features.home.domain.RateSummary
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.preferences.domain.formatMoney
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.valentinilk.shimmer.shimmer

/**
 * Represents the state of the Home screen in the application, encapsulating all UI-related data.
 */
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
        intent: (HomeIntent) -> Unit
    ) {
        val pullToRefreshState = remember {
            object : PullToRefreshState {
                private val anim = Animatable(0f, Float.VectorConverter)
                override val distanceFraction get() = anim.value
                override val isAnimating: Boolean
                    get() = anim.isRunning

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

        LaunchedEffect(displayLoading) {
            onFabConfigChanged.invoke(!displayLoading, elapsedTone) { intent(HomeIntent.AddSmoke) }
        }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    onFabConfigChanged.invoke(available.y > 1, elapsedTone) { intent(HomeIntent.AddSmoke) }
                    return Offset.Zero
                }
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
            }
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
                intent = intent,
                isLoading = displayLoading
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
    intent: (HomeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GreetingSection(
                greetingTitle = greetingTitle,
                greetingMessage = greetingMessage,
                canStartNewDay = canStartNewDay,
                onStartNewDay = { intent(HomeIntent.StartNewDay) },
                onHistoryClick = { intent(HomeIntent.OnClickHistory) },
                isLoading = isLoading,
            )
        }
        item {
            TimeSinceLastCigaretteSection(
                timeSinceLastCigarette = timeSinceLastCigarette,
                isLoading = isLoading,
                elapsedTone = elapsedTone,
            )
        }
        item {
            DashboardMetricGrid(
                smokesPerDay = smokesPerDay,
                smokesPerWeek = smokesPerWeek,
                smokesPerMonth = smokesPerMonth,
                financialSummary = financialSummary,
                rateSummary = rateSummary,
                gamificationSummary = gamificationSummary,
                isLoading = isLoading,
                onHistoryClick = { intent(HomeIntent.OnClickHistory) },
            )
        }
    }
}

@Composable
private fun TimeSinceLastCigaretteSection(
    timeSinceLastCigarette: Pair<Long, Long>?,
    isLoading: Boolean,
    elapsedTone: ElapsedTone,
) {
    Spacer(modifier = Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .background(
                color = elapsedTone.containerColor(),
                shape = MaterialTheme.shapes.medium
            )
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = stringResource(id = R.string.home_since_your_last_cigarette),
                style = MaterialTheme.typography.labelSmall,
                color = elapsedTone.contentColor(),
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .size(100.dp, 26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmer()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                )
            } else {
                Text(
                    text = timeSinceLastCigarette?.let { (hours, minutes) ->
                        listOfNotNull(
                            stringResource(
                                id = R.string.home_smoked_after_hours_short,
                                hours.toInt()
                            ).takeIf { hours > 0 },
                            stringResource(
                                id = R.string.home_smoked_after_minutes_short,
                                minutes.toInt()
                            )
                        ).joinToString(", ")
                    } ?: "--",
                    style = MaterialTheme.typography.titleLarge,
                    color = elapsedTone.contentColor(),
                )
            }
        }
        Image(
            modifier = Modifier.size(96.dp),
            painter = painterResource(id = R.drawable.il_cigarette_background),
            contentDescription = null
        )
    }
}

@Composable
private fun GreetingSection(
    greetingTitle: String?,
    greetingMessage: String?,
    canStartNewDay: Boolean,
    onStartNewDay: () -> Unit,
    onHistoryClick: () -> Unit,
    isLoading: Boolean,
) {
    if (greetingTitle == null && greetingMessage == null && !canStartNewDay) return
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            greetingTitle?.let {
                Text(text = it, style = MaterialTheme.typography.titleMedium)
            }
            greetingMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onHistoryClick,
                    enabled = !isLoading,
                ) {
                    Text(text = "Open history")
                }
                if (canStartNewDay) {
                    TextButton(
                        onClick = onStartNewDay,
                        enabled = !isLoading,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                    ) {
                        Text(text = "Start new day")
                    }
                }
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
private fun DashboardMetricGrid(
    smokesPerDay: Int?,
    smokesPerWeek: Int?,
    smokesPerMonth: Int?,
    financialSummary: FinancialSummary?,
    rateSummary: RateSummary?,
    gamificationSummary: GamificationSummary?,
    isLoading: Boolean,
    onHistoryClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardMetricCard(
                modifier = Modifier.weight(1f),
                title = "Today",
                value = smokesPerDay?.toString(),
                supporting = "Current day bucket",
                isLoading = isLoading,
                onClick = onHistoryClick,
            )
            DashboardMetricCard(
                modifier = Modifier.weight(1f),
                title = "Week",
                value = smokesPerWeek?.toString(),
                supporting = rateSummary?.let { "%.1f / day".format(it.averageSmokesPerDayWeek) },
                isLoading = isLoading,
                onClick = onHistoryClick,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardMetricCard(
                modifier = Modifier.weight(1f),
                title = "Month",
                value = smokesPerMonth?.toString(),
                supporting = rateSummary?.let { "%.1f / day".format(it.averageSmokesPerDayMonth) },
                isLoading = isLoading,
                onClick = onHistoryClick,
            )
            DashboardMetricCard(
                modifier = Modifier.weight(1f),
                title = "Avg gap today",
                value = rateSummary?.averageIntervalMinutesToday?.toGapLabel(),
                supporting = rateSummary?.latestIntervalMinutes?.let { "Latest ${it.toGapLabel()}" },
                isLoading = isLoading,
                tone = rateSummary?.averageIntervalMinutesToday?.let(::elapsedToneFromMinutes),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardMetricCard(
                modifier = Modifier.weight(1f),
                title = "Spent today",
                value = financialSummary?.spentToday?.formatMoney(financialSummary.currencySymbol),
                supporting = financialSummary?.let {
                    "Week ${it.spentWeek.formatMoney(it.currencySymbol)}"
                },
                isLoading = isLoading,
            )
            DashboardMetricCard(
                modifier = Modifier.weight(1f),
                title = "Points",
                value = gamificationSummary?.points?.toString(),
                supporting = gamificationSummary?.let { "Next ${it.nextMilestoneHours}h" },
                isLoading = isLoading,
            )
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String?,
    supporting: String?,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    tone: ElapsedTone? = null,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .aspectRatio(1.15f)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = tone?.containerColor()?.copy(alpha = 0.58f) ?: MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = tone?.contentColor()?.copy(alpha = 0.8f) ?: MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .size(96.dp, 24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmer()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                )
            } else {
                Text(
                    text = value ?: "--",
                    style = MaterialTheme.typography.headlineMedium,
                    color = tone?.contentColor() ?: MaterialTheme.colorScheme.onSurface,
                )
            }
            supporting?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = tone?.contentColor()?.copy(alpha = 0.8f) ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun Int.toGapLabel(): String = when {
    this >= 60 -> "${this / 60}h ${this % 60}m"
    else -> "${this}m"
}

private fun elapsedToneFromMinutes(minutes: Int): ElapsedTone = when {
    minutes >= 180 -> ElapsedTone.Calm
    minutes >= 90 -> ElapsedTone.Caution
    minutes >= 45 -> ElapsedTone.Warning
    else -> ElapsedTone.Urgent
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
        HomeViewState().Compose({ _, _, _ -> }, {})
    }
}
