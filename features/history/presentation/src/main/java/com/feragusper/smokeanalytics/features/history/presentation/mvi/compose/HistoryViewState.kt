package com.feragusper.smokeanalytics.features.history.presentation.mvi.compose

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.dateFormatted
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.DatePickerDialog
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.EmptySmokes
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.Stat
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.SwipeToDismissRow
import java.time.LocalDateTime

data class HistoryViewState(
    internal val displayLoading: Boolean = false,
    internal val smokes: List<Smoke>? = null,
    internal val error: HistoryResult.Error? = null,
    internal val selectedDate: LocalDateTime = LocalDateTime.now(),
) : MVIViewState<HistoryIntent> {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Compose(intent: (HistoryIntent) -> Unit) {
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
                        onClick = {
                            requireNotNull(smokes)
                            intent(HistoryIntent.AddSmoke(selectedDate))
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
                var showDatePicker by remember { mutableStateOf(false) }
                if (showDatePicker) {
                    DatePickerDialog(
                        initialDate = selectedDate,
                        onConfirm = { date ->
                            intent(HistoryIntent.FetchSmokes(date))
                        },
                        onDismiss = { showDatePicker = false }
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(contentPadding)
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = {
                            intent(HistoryIntent.FetchSmokes(selectedDate.plusDays(-1)))
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = ""
                            )
                        }
                        Text(
                            modifier = Modifier.clickable { showDatePicker = true },
                            text = selectedDate.dateFormatted()
                        )
                        IconButton(onClick = {
                            intent(HistoryIntent.FetchSmokes(selectedDate.plusDays(1)))
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = ""
                            )
                        }
                    }
                    Stat(
                        titleResourceId = R.string.history_smoked,
                        count = smokes?.size ?: 0
                    )

                    smokes?.takeIf { it.isNotEmpty() }?.let {
                        LazyColumn(
                            modifier = Modifier
                                .nestedScroll(nestedScrollConnection)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
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
                    } ?: run { EmptySmokes() }
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
        }
    }

}