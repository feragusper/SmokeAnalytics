package com.feragusper.smokeanalytics.features.home.presentation.mvi

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.home.presentation.R
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.presentation.Stat
import com.feragusper.smokeanalytics.libraries.smokes.presentation.SwipeToDismissRow
import java.util.Date

data class HomeViewState(
    internal val displayLoading: Boolean = false,
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

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Compose(intent: (HomeIntent) -> Unit) {
        val snackbarHostState = remember { SnackbarHostState() }
        val isFABVisible = rememberSaveable { mutableStateOf(true) }
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // Hide FAB
                    if (available.y < -1) {
                        isFABVisible.value = false
                    }

                    // Show FAB
                    if (available.y > 1) {
                        isFABVisible.value = true
                    }

                    return Offset.Zero
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
                        onClick = { intent(HomeIntent.AddSmoke) },
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
            },
        ) { contentPadding ->
            if (displayLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val state = rememberPullToRefreshState()
                if (state.isRefreshing) {
                    LaunchedEffect(true) {
                        intent(HomeIntent.FetchSmokes)
                    }
                }

                Box(Modifier.nestedScroll(state.nestedScrollConnection)) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(contentPadding)
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                            .nestedScroll(nestedScrollConnection)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        stickyHeader {
                            Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    smokesPerDay?.let { smokesPerDay ->
                                        Stat(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    intent(HomeIntent.OnClickHistory)
                                                },
                                            titleResourceId = R.string.home_label_per_day,
                                            count = smokesPerDay
                                        )
                                    }
                                    smokesPerWeek?.let { smokesPerWeek ->
                                        Stat(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    intent(HomeIntent.OnClickHistory)
                                                },
                                            titleResourceId = R.string.home_label_per_week,
                                            count = smokesPerWeek
                                        )
                                    }
                                    smokesPerMonth?.let { smokesPerMonth ->
                                        Stat(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    intent(HomeIntent.OnClickHistory)
                                                },
                                            titleResourceId = R.string.home_label_per_month,
                                            count = smokesPerMonth
                                        )
                                    }
                                }
                                timeSinceLastCigarette?.let { timeSinceLastCigarette ->
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
                                Text(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    text = stringResource(id = R.string.home_smoked_today),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        }
                        latestSmokes.takeIf { !it.isNullOrEmpty() }?.let {
                            items(it) { smoke ->
                                SwipeToDismissRow(
                                    date = smoke.date,
                                    timeElapsedSincePreviousSmoke = smoke.timeElapsedSincePreviousSmoke,
                                    onDelete = { intent(HomeIntent.DeleteSmoke(smoke.id)) },
                                    fullDateTimeEdit = false,
                                    onEdit = { date ->
                                        intent(
                                            HomeIntent.EditSmoke(
                                                smoke.id,
                                                date
                                            )
                                        )
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                    PullToRefreshContainer(
                        modifier = Modifier.align(Alignment.TopCenter),
                        state = state,
                    )
                }
            }
        }

        val context = LocalContext.current
        LaunchedEffect(error) {
            error?.let {
                when (it) {
                    HomeResult.Error.Generic -> snackbarHostState.showSnackbar(
                        context.getString(
                            R.string.error_generic
                        )
                    )

                    HomeResult.Error.NotLoggedIn -> Toast.makeText(
                        context,
                        context.getString(R.string.error_not_logged_in),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}

@CombinedPreviews
@Composable
private fun HomeViewLoadingPreview() {
    SmokeAnalyticsTheme {
        HomeViewState(
            displayLoading = true,
        ).Compose {}
    }
}

@CombinedPreviews
@Composable
private fun HomeViewSuccessPreview() {
    SmokeAnalyticsTheme {
        HomeViewState(
            smokesPerDay = 10,
            smokesPerWeek = 20,
            smokesPerMonth = 30,
            timeSinceLastCigarette = 1L to 30L,
            latestSmokes = buildList {
                repeat(4) {
                    add(
                        Smoke(
                            id = "123",
                            date = Date(),
                            timeElapsedSincePreviousSmoke = 1L to 30L
                        )
                    )
                }
            },
        ).Compose {}
    }
}

