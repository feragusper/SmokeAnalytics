package com.feragusper.smokeanalytics.features.history.presentation.mvi.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.history.presentation.R
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.DatePickerDialog
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.EmptySmokes
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.Stat
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.SwipeToDismissRow
import com.valentinilk.shimmer.shimmer
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class HistoryViewState(
    internal val displayLoading: Boolean = false,
    internal val smokes: List<Smoke>? = null,
    internal val monthCounts: Map<Int, Int> = emptyMap(),
    internal val error: HistoryResult.Error? = null,
    internal val selectedDate: Instant = Clock.System.now(),
) : MVIViewState<HistoryIntent> {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Compose(intent: (HistoryIntent) -> Unit) {
        val snackbarHostState = remember { SnackbarHostState() }
        val isFABVisible = rememberSaveable { mutableStateOf(true) }
        val timeZone = remember { TimeZone.currentSystemDefault() }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y < -1) isFABVisible.value = false
                    if (available.y > 1) isFABVisible.value = true
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
                        onClick = { intent(HistoryIntent.AddSmoke(selectedDate)) },
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_cigarette),
                                contentDescription = null
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
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { contentPadding ->
            var showDatePicker by remember { mutableStateOf(false) }
            var calendarMode by rememberSaveable { mutableStateOf(false) }

            if (showDatePicker) {
                DatePickerDialog(
                    initialDate = selectedDate,
                    onConfirm = { dateInstant ->
                        showDatePicker = false
                        intent(HistoryIntent.FetchSmokes(dateInstant))
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
                        intent(HistoryIntent.FetchSmokes(selectedDate.minusDays(1, timeZone)))
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }

                    Text(
                        modifier = Modifier.clickable { showDatePicker = true },
                        text = selectedDate.toLocalDateTime(timeZone).dateFormattedUi(),
                    )

                    IconButton(onClick = {
                        intent(HistoryIntent.FetchSmokes(selectedDate.plusDays(1, timeZone)))
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !calendarMode,
                        onClick = { calendarMode = false },
                        label = { Text("List") },
                    )
                    FilterChip(
                        selected = calendarMode,
                        onClick = { calendarMode = true },
                        label = { Text("Calendar") },
                    )
                }

                Stat(
                    titleResourceId = R.string.history_smoked,
                    count = smokes?.size ?: 0,
                    isLoading = displayLoading
                )

                if (calendarMode && !displayLoading) {
                    CalendarMonthCard(
                        selectedLocalDate = selectedDate.toLocalDateTime(timeZone).date,
                        monthCounts = monthCounts,
                        onPickDay = { picked ->
                            intent(HistoryIntent.FetchSmokes(picked.atStartOfDayIn(timeZone)))
                        }
                    )
                }

                if (displayLoading) {
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
                } else if (!smokes.isNullOrEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(smokes) { smoke ->
                            SwipeToDismissRow(
                                date = smoke.date,
                                timeElapsedSincePreviousSmoke = smoke.timeElapsedSincePreviousSmoke,
                                onDelete = { intent(HistoryIntent.DeleteSmoke(smoke.id)) },
                                fullDateTimeEdit = true,
                                onEdit = { editedInstant ->
                                    intent(HistoryIntent.EditSmoke(smoke.id, editedInstant))
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                } else {
                    EmptySmokes()
                }
            }
        }
    }
}

private fun Instant.plusDays(days: Int, timeZone: TimeZone): Instant =
    this.plus(days, DateTimeUnit.DAY, timeZone)

private fun Instant.minusDays(days: Int, timeZone: TimeZone): Instant =
    this.plus(-days, DateTimeUnit.DAY, timeZone)

private fun kotlinx.datetime.LocalDateTime.dateFormattedUi(): String {
    // Cambiá el formato si querés. Esto evita depender de tu extension vieja de java.time.
    val day = "%02d".format(dayOfMonth)
    val month = "%02d".format(monthNumber)
    return "$day/$month/$year"
}

@Composable
private fun CalendarMonthCard(
    selectedLocalDate: LocalDate,
    monthCounts: Map<Int, Int>,
    onPickDay: (LocalDate) -> Unit,
) {
    val monthStart = LocalDate(selectedLocalDate.year, selectedLocalDate.monthNumber, 1)
    val nextMonthStart = monthStart.plus(DatePeriod(months = 1))
    val daysInMonth = nextMonthStart.plus(DatePeriod(days = -1)).dayOfMonth
    val leadingEmptySlots = monthStart.dayOfWeek.isoDayNumber - 1
    val maxCount = monthCounts.values.maxOrNull() ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = selectedLocalDate.toUiMonthYear(),
            style = MaterialTheme.typography.titleSmall,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                Box(modifier = Modifier.width(42.dp), contentAlignment = Alignment.Center) {
                    Text(text = label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        val totalSlots = leadingEmptySlots + daysInMonth
        val rows = (totalSlots + 6) / 7
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (column in 0 until 7) {
                    val slot = row * 7 + column
                    val day = slot - leadingEmptySlots + 1
                    if (day !in 1..daysInMonth) {
                        Spacer(modifier = Modifier.width(42.dp))
                    } else {
                        val count = monthCounts[day] ?: 0
                        val date = LocalDate(selectedLocalDate.year, selectedLocalDate.monthNumber, day)
                        val background = when {
                            date == selectedLocalDate -> MaterialTheme.colorScheme.primaryContainer
                            count == 0 -> MaterialTheme.colorScheme.surface
                            maxCount <= 1 -> MaterialTheme.colorScheme.secondaryContainer
                            count >= maxCount -> MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                        }
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(background)
                                .clickable { onPickDay(date) }
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = day.toString(), style = MaterialTheme.typography.labelLarge)
                                if (count > 0) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

private fun LocalDate.toUiMonthYear(): String {
    val monthName = month.name.lowercase().replaceFirstChar { it.titlecase() }
    return "$monthName $year"
}
