package com.feragusper.smokeanalytics.features.home.presentation.mvi.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.home.presentation.R
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.features.home.presentation.mvi.compose.HomeViewState.TestTags.Companion.BUTTON_ADD_SMOKE
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.EmptySmokes
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.Stat
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.SwipeToDismissRow
import java.time.LocalDateTime

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
    internal val error: HomeResult.Error? = null,
) : MVIViewState<HomeIntent> {

    interface TestTags {
        companion object {
            const val BUTTON_ADD_SMOKE = "buttonAddSmoke"
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Compose(intent: (HomeIntent) -> Unit) {
        val snackbarHostState = remember { SnackbarHostState() }
        val isFABVisible = rememberSaveable { mutableStateOf(true) }

        // Handle FAB visibility on scroll
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    when {
                        available.y < -1 -> isFABVisible.value = false
                        available.y > 1 -> isFABVisible.value = true
                    }
                    return Offset.Zero
                }
            }
        }

        // Pull to refresh state management
        val pullToRefreshState = remember {
            object : PullToRefreshState {
                private val anim = Animatable(0f, Float.VectorConverter)
                override val distanceFraction get() = anim.value
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

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = isFABVisible.value && !displayLoading,
                    enter = slideInVertically(initialOffsetY = { it * 2 }),
                    exit = slideOutVertically(targetOffsetY = { it * 2 }),
                ) {
                    FloatingActionButton(
                        modifier = Modifier.testTag(BUTTON_ADD_SMOKE),
                        onClick = { intent(HomeIntent.AddSmoke) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_cigarette),
                                contentDescription = ""
                            )
                            Text(
                                text = stringResource(R.string.home_button_track),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        ) { contentPadding ->
            PullToRefreshBox(
                isRefreshing = displayRefreshLoading,
                onRefresh = { intent(HomeIntent.FetchSmokes) },
                state = pullToRefreshState,
                modifier = Modifier.padding(contentPadding),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = displayRefreshLoading,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            ) {
                if (displayLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    HomeContent(
                        nestedScrollConnection = nestedScrollConnection,
                        smokesPerDay = smokesPerDay,
                        smokesPerWeek = smokesPerWeek,
                        smokesPerMonth = smokesPerMonth,
                        timeSinceLastCigarette = timeSinceLastCigarette,
                        latestSmokes = latestSmokes,
                        intent = intent
                    )
                }
            }
        }
    }
}

@Composable
fun HomeContent(
    nestedScrollConnection: NestedScrollConnection,
    smokesPerDay: Int?,
    smokesPerWeek: Int?,
    smokesPerMonth: Int?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    latestSmokes: List<Smoke>?,
    intent: (HomeIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        StatsSection(
            smokesPerDay = smokesPerDay,
            smokesPerWeek = smokesPerWeek,
            smokesPerMonth = smokesPerMonth,
            onHistoryClick = { intent(HomeIntent.OnClickHistory) }
        )

        TimeSinceLastCigaretteSection(timeSinceLastCigarette)

        LatestSmokesSection(
            latestSmokes = latestSmokes,
            nestedScrollConnection = nestedScrollConnection,
            onEdit = { id, date -> intent(HomeIntent.EditSmoke(id, date)) },
            onDelete = { id -> intent(HomeIntent.DeleteSmoke(id)) }
        )
    }
}

@Composable
private fun StatsSection(
    smokesPerDay: Int?,
    smokesPerWeek: Int?,
    smokesPerMonth: Int?,
    onHistoryClick: () -> Unit
) {
    Column(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            smokesPerDay?.let {
                Stat(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onHistoryClick() },
                    titleResourceId = R.string.home_label_per_day,
                    count = it
                )
            }
            smokesPerWeek?.let {
                Stat(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onHistoryClick() },
                    titleResourceId = R.string.home_label_per_week,
                    count = it
                )
            }
            smokesPerMonth?.let {
                Stat(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onHistoryClick() },
                    titleResourceId = R.string.home_label_per_month,
                    count = it
                )
            }
        }
    }
}

@Composable
private fun TimeSinceLastCigaretteSection(
    timeSinceLastCigarette: Pair<Long, Long>?
) {
    timeSinceLastCigarette?.let {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(
                    4.dp,
                    Alignment.CenterVertically
                ),
            ) {
                Text(
                    text = stringResource(id = R.string.home_since_your_last_cigarette),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = timeSinceLastCigarette.let { (hours, minutes) ->
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
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Image(
                painter = painterResource(id = R.drawable.il_cigarette_background),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun LatestSmokesSection(
    latestSmokes: List<Smoke>?,
    nestedScrollConnection: NestedScrollConnection,
    onEdit: (String, LocalDateTime) -> Unit,
    onDelete: (String) -> Unit
) {
    Text(
        modifier = Modifier.padding(vertical = 12.dp),
        text = stringResource(id = R.string.home_smoked_today),
        style = MaterialTheme.typography.titleSmall,
    )

    latestSmokes?.takeIf { it.isNotEmpty() }?.let {
        LazyColumn(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(it) { smoke ->
                SwipeToDismissRow(
                    date = smoke.date,
                    timeElapsedSincePreviousSmoke = smoke.timeElapsedSincePreviousSmoke,
                    onDelete = { onDelete(smoke.id) },
                    fullDateTimeEdit = false,
                    onEdit = { date -> onEdit(smoke.id, date) }
                )
                HorizontalDivider()
            }
        }
    } ?: run {
        EmptySmokes()
    }
}


@CombinedPreviews
@Composable
private fun HomeViewLoadingPreview() {
    SmokeAnalyticsTheme {
        HomeViewState(displayLoading = true).Compose {}
    }
}
