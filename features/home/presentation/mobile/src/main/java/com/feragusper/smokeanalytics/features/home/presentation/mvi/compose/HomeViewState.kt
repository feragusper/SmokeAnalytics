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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.EmptySmokes
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.Stat
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.SwipeToDismissRow
import com.valentinilk.shimmer.shimmer
import kotlinx.datetime.Instant

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
    internal val greetingTitle: String? = null,
    internal val greetingMessage: String? = null,
    internal val financialSummary: FinancialSummary? = null,
    internal val gamificationSummary: GamificationSummary? = null,
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
                latestSmokes = latestSmokes,
                greetingTitle = greetingTitle,
                greetingMessage = greetingMessage,
                financialSummary = financialSummary,
                gamificationSummary = gamificationSummary,
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
    latestSmokes: List<Smoke>?,
    greetingTitle: String?,
    greetingMessage: String?,
    financialSummary: FinancialSummary?,
    gamificationSummary: GamificationSummary?,
    elapsedTone: ElapsedTone,
    isLoading: Boolean,
    intent: (HomeIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        GreetingSection(
            greetingTitle = greetingTitle,
            greetingMessage = greetingMessage,
            financialSummary = financialSummary,
            gamificationSummary = gamificationSummary,
        )
        StatsSection(
            smokesPerDay = smokesPerDay,
            smokesPerWeek = smokesPerWeek,
            smokesPerMonth = smokesPerMonth,
            isLoading = isLoading,
            onHistoryClick = { intent(HomeIntent.OnClickHistory) }
        )

        TimeSinceLastCigaretteSection(
            timeSinceLastCigarette = timeSinceLastCigarette,
            isLoading = isLoading,
            elapsedTone = elapsedTone,
        )

        LatestSmokesSection(
            latestSmokes = latestSmokes,
            nestedScrollConnection = nestedScrollConnection,
            isLoading = isLoading,
            onEdit = { id, instant -> intent(HomeIntent.EditSmoke(id, instant)) },
            onDelete = { id -> intent(HomeIntent.DeleteSmoke(id)) }
        )
    }
}

@Composable
private fun StatsSection(
    smokesPerDay: Int?,
    smokesPerWeek: Int?,
    smokesPerMonth: Int?,
    isLoading: Boolean,
    onHistoryClick: () -> Unit
) {
    Column(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onHistoryClick() },
                titleResourceId = R.string.home_label_per_day,
                count = smokesPerDay,
                isLoading = isLoading
            )
            Stat(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onHistoryClick() },
                titleResourceId = R.string.home_label_per_week,
                count = smokesPerWeek,
                isLoading = isLoading
            )
            Stat(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onHistoryClick() },
                titleResourceId = R.string.home_label_per_month,
                count = smokesPerMonth,
                isLoading = isLoading
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
            painter = painterResource(id = R.drawable.il_cigarette_background),
            contentDescription = null
        )
    }
}

@Composable
private fun GreetingSection(
    greetingTitle: String?,
    greetingMessage: String?,
    financialSummary: FinancialSummary?,
    gamificationSummary: GamificationSummary?,
) {
    if (greetingTitle == null && financialSummary == null && gamificationSummary == null) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            greetingTitle?.let {
                Text(text = it, style = MaterialTheme.typography.titleMedium)
            }
            greetingMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            financialSummary?.let {
                Text(
                    text = "Spent today ${"%.2f".format(it.spentToday)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            gamificationSummary?.let {
                Text(
                    text = "Points ${it.points} · Streak ${it.currentStreakHours}h",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun ElapsedTone.containerColor(): Color = when (this) {
    ElapsedTone.Urgent -> MaterialTheme.colorScheme.errorContainer
    ElapsedTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer
    ElapsedTone.Caution -> MaterialTheme.colorScheme.secondaryContainer
    ElapsedTone.Calm -> MaterialTheme.colorScheme.primaryContainer
}

@Composable
private fun ElapsedTone.contentColor(): Color = when (this) {
    ElapsedTone.Urgent -> MaterialTheme.colorScheme.onErrorContainer
    ElapsedTone.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
    ElapsedTone.Caution -> MaterialTheme.colorScheme.onSecondaryContainer
    ElapsedTone.Calm -> MaterialTheme.colorScheme.onPrimaryContainer
}

@Composable
private fun LatestSmokesSection(
    latestSmokes: List<Smoke>?,
    nestedScrollConnection: NestedScrollConnection,
    isLoading: Boolean,
    onEdit: (String, Instant) -> Unit,
    onDelete: (String) -> Unit
) {
    Text(
        modifier = Modifier.padding(vertical = 12.dp),
        text = stringResource(id = R.string.home_smoked_today),
        style = MaterialTheme.typography.titleSmall,
    )

    if (isLoading) {
        LazyColumn(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            items(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmer()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                )
            }
        }
    } else if (!latestSmokes.isNullOrEmpty()) {
        LazyColumn(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(latestSmokes) { smoke ->
                SwipeToDismissRow(
                    date = smoke.date,
                    timeElapsedSincePreviousSmoke = smoke.timeElapsedSincePreviousSmoke,
                    onDelete = { onDelete(smoke.id) },
                    fullDateTimeEdit = false,
                    onEdit = { editedInstant ->
                        onEdit(smoke.id, editedInstant)
                    }
                )
                HorizontalDivider()
            }
        }
    } else {
        EmptySmokes()
    }
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
