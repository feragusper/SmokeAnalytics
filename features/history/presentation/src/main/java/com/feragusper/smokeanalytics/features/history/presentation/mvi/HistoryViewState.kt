package com.feragusper.smokeanalytics.features.history.presentation.mvi

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.history.presentation.R
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.dateFormatted
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.theme.SmokeAnalyticsTheme
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.presentation.Stat
import com.feragusper.smokeanalytics.libraries.smokes.presentation.SwipeToDismissRow
import kotlinx.coroutines.launch
import java.util.Date

data class HistoryViewState(
    internal val displayLoading: Boolean = false,
    internal val smokes: Map<Date, List<Smoke>>? = null,
    internal val error: HistoryResult.Error? = null,
) : MVIViewState<HistoryIntent> {

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Compose(intent: (HistoryIntent) -> Unit) {
        val snackbarHostState = remember { SnackbarHostState() }
        val pagerState = smokes?.let {
            rememberPagerState(
                initialPage = it.keys.size - 1,
                pageCount = {
                    it.keys.size
                }
            )
        }
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
                        onClick = {
                            requireNotNull(pagerState)
                            requireNotNull(smokes)
                            intent(HistoryIntent.AddSmoke(pagerState.currentPage.let {
                                smokes.keys.elementAt(
                                    it
                                )
                            }))
                        },
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
                                text = stringResource(R.string.history_button_track),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.history_smoked),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { intent(HistoryIntent.NavigateUp) }) {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.AutoMirrored.Default.ArrowBack),
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
        ) { contentPadding ->
            if (displayLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                pagerState?.let { pagerState ->
                    requireNotNull(smokes)
                    Column(modifier = Modifier.padding(contentPadding)) {
                        val scope = rememberCoroutineScope()
                        PagerNavigationHeader(
                            onClickPrevious = {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            },
                            onClickNext = {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            },
                            date = pagerState.currentPage.let { smokes.keys.elementAt(it) },
                            nextEnabled = pagerState.currentPage < smokes.keys.size - 1,
                            previousEnabled = pagerState.currentPage > 0
                        )
                        HorizontalPager(state = pagerState) { index ->
                            smokes.entries.elementAt(index).let { (_, smokes) ->
                                LazyColumn(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .nestedScroll(nestedScrollConnection)
                                        .fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    stickyHeader {
                                        Stat(
                                            titleResourceId = R.string.history_smoked,
                                            count = smokes.size
                                        )
                                    }
                                    items(smokes) { smoke ->
                                        SwipeToDismissRow(
                                            date = smoke.date,
                                            timeElapsedSincePreviousSmoke = smoke.timeElapsedSincePreviousSmoke,
                                            onDelete = { intent(HistoryIntent.DeleteSmoke(smoke.id)) },
                                            fullDateTimeEdit = true,
                                            onEdit = { date ->
                                                intent(
                                                    HistoryIntent.EditSmoke(
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
                        }
                    }
                }
            }
        }

        val context = LocalContext.current
        LaunchedEffect(error) {
            error?.let {
                when (it) {
                    HistoryResult.Error.Generic -> snackbarHostState.showSnackbar(
                        context.getString(
                            R.string.error_generic
                        )
                    )

                    HistoryResult.Error.NotLoggedIn -> Toast.makeText(
                        context,
                        context.getString(R.string.error_not_logged_in),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @Composable
    private fun PagerNavigationHeader(
        onClickPrevious: () -> Unit,
        onClickNext: () -> Unit,
        date: Date,
        nextEnabled: Boolean,
        previousEnabled: Boolean
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                enabled = previousEnabled,
                onClick = onClickPrevious
            ) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.AutoMirrored.Filled.ArrowBack),
                    contentDescription = null
                )
            }
            Text(
                text = date.dateFormatted(),
                style = MaterialTheme.typography.titleSmall,
            )
            Button(
                enabled = nextEnabled,
                onClick = onClickNext
            ) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.AutoMirrored.Filled.ArrowForward),
                    contentDescription = null
                )
            }
        }
    }

}

@CombinedPreviews
@Composable
private fun HomeViewLoadingPreview() {
    SmokeAnalyticsTheme {
        HistoryViewState(
            displayLoading = true,
        ).Compose {}
    }
}

@CombinedPreviews
@Composable
private fun HomeViewSuccessPreview() {
    SmokeAnalyticsTheme {
        HistoryViewState(
            smokes = buildMap {
                repeat(4) {
                    put(
                        Date(),
                        buildList {
                            repeat(4) {
                                add(
                                    Smoke(
                                        id = "123",
                                        date = Date(),
                                        timeElapsedSincePreviousSmoke = 1L to 30L
                                    )
                                )
                            }
                        }
                    )
                }
            },
        ).Compose {}
    }
}

