package com.feragusper.smokeanalytics.features.history.presentation.mvi.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.history.presentation.R
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.DatePickerDialog
import com.feragusper.smokeanalytics.libraries.smokes.presentation.compose.EmptySmokes
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
    internal val pendingSmokeId: String? = null,
    internal val pendingAction: com.feragusper.smokeanalytics.features.history.presentation.HistoryPendingAction? = null,
    internal val rowInteractionEpoch: Int = 0,
) : MVIViewState<HistoryIntent> {

    @Composable
    fun Compose(
        showNavigationIcon: Boolean = true,
        intent: (HistoryIntent) -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val timeZone = remember { TimeZone.currentSystemDefault() }
        var showDatePicker by remember { mutableStateOf(false) }
        var calendarMode by rememberSaveable { mutableStateOf(true) }

        val selectedLocalDate = selectedDate.toLocalDateTime(timeZone).date
        val dateLabel = selectedLocalDate.toUiMonthDay()
        val entriesCount = smokes?.size ?: 0
        val trendValue = remember(monthCounts) { monthTrendPercent(monthCounts) }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { contentPadding ->
            if (showDatePicker) {
                DatePickerDialog(
                    initialDate = selectedDate,
                    onConfirm = { dateInstant ->
                        showDatePicker = false
                        intent(HistoryIntent.FetchSmokes(dateInstant))
                    },
                    onDismiss = { showDatePicker = false },
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(contentPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    ArchiveHeader(
                        showNavigationIcon = showNavigationIcon,
                        onNavigateUp = { intent(HistoryIntent.NavigateUp) },
                        entriesCount = entriesCount,
                        displayLoading = displayLoading,
                        hasCachedEntries = smokes != null,
                    )
                }

                item {
                    ArchiveControlsCard(
                        calendarMode = calendarMode,
                        displayLoading = displayLoading,
                        selectedLocalDate = selectedLocalDate,
                        entriesCount = entriesCount,
                        onSelectCalendar = { calendarMode = true },
                        onSelectList = { calendarMode = false },
                        onAdd = { intent(HistoryIntent.AddSmoke(selectedDate)) },
                        onPrevious = { intent(HistoryIntent.FetchSmokes(selectedDate.minusDays(1, timeZone))) },
                        onNext = { intent(HistoryIntent.FetchSmokes(selectedDate.plusDays(1, timeZone))) },
                        onPickDate = { showDatePicker = true },
                    )
                }

                item {
                    if (calendarMode) {
                        ArchiveCalendarCard(
                            selectedLocalDate = selectedLocalDate,
                            monthCounts = monthCounts,
                            onShiftMonth = { amount ->
                                val shifted = selectedLocalDate.plus(DatePeriod(months = amount))
                                intent(
                                    HistoryIntent.FetchSmokes(
                                        LocalDate(shifted.year, shifted.monthNumber, 1).atStartOfDayIn(timeZone)
                                    )
                                )
                            },
                            onPickDay = { picked ->
                                intent(HistoryIntent.FetchSmokes(picked.atStartOfDayIn(timeZone)))
                            },
                        )
                    } else {
                        ArchiveDaySummaryCard(
                            selectedLocalDate = selectedLocalDate,
                            entriesCount = entriesCount,
                        )
                    }
                }

                error?.let { currentError ->
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = if (currentError == HistoryResult.Error.NotLoggedIn) "Session required" else "Could not refresh archive",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    text = if (currentError == HistoryResult.Error.NotLoggedIn) {
                                        "Sign back in to keep the archive synced."
                                    } else {
                                        "Showing the last available state while the selected day could not be refreshed."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                when {
                    displayLoading && smokes == null -> {
                        items(4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .shimmer()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }

                    !smokes.isNullOrEmpty() -> {
                        item {
                            ArchiveDaySummaryCard(
                                selectedLocalDate = selectedLocalDate,
                                entriesCount = entriesCount,
                            )
                        }

                        items(
                            items = smokes,
                            key = { smoke -> "${smoke.id}-${rowInteractionEpoch}" }
                        ) { smoke ->
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                ),
                            ) {
                                SwipeToDismissRow(
                                    itemKey = smoke.id,
                                    date = smoke.date,
                                    timeElapsedSincePreviousSmoke = smoke.timeElapsedSincePreviousSmoke,
                                    onDelete = { intent(HistoryIntent.DeleteSmoke(smoke.id)) },
                                    fullDateTimeEdit = true,
                                    onEdit = { editedInstant -> intent(HistoryIntent.EditSmoke(smoke.id, editedInstant)) },
                                    isPending = pendingSmokeId == smoke.id,
                                    pendingLabel = when (pendingAction) {
                                        com.feragusper.smokeanalytics.features.history.presentation.HistoryPendingAction.Editing -> "Saving edit…"
                                        com.feragusper.smokeanalytics.features.history.presentation.HistoryPendingAction.Deleting -> "Deleting…"
                                        null -> null
                                    },
                                )
                            }
                        }
                    }

                    else -> {
                        item { EmptySmokes() }
                    }
                }

                item {
                    TrendCard(trendValue = trendValue)
                }
            }
        }
    }
}

@Composable
private fun ArchiveHeader(
    showNavigationIcon: Boolean,
    onNavigateUp: () -> Unit,
    entriesCount: Int,
    displayLoading: Boolean,
    hasCachedEntries: Boolean,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (showNavigationIcon) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "The Archive",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = if (displayLoading && hasCachedEntries) "Refreshing" else "$entriesCount entries",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Text(
                text = "Browse the calendar, inspect one day, and edit the smoking log without leaving the main shell.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HistoryDateBar(
    selectedLocalDate: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPickDate: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = selectedLocalDate.toUiMonthYear(),
            style = MaterialTheme.typography.titleLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                text = selectedLocalDate.toUiMonthDay(),
                modifier = Modifier.clickable(onClick = onPickDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun ArchiveControlsCard(
    calendarMode: Boolean,
    displayLoading: Boolean,
    selectedLocalDate: LocalDate,
    entriesCount: Int,
    onSelectCalendar: () -> Unit,
    onSelectList: () -> Unit,
    onAdd: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPickDate: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (calendarMode) "Browse the month" else "Inspect one day",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (calendarMode) {
                        "Calendar mode is best for spotting denser days before drilling into a specific date."
                    } else {
                        "List mode keeps the selected day open for editing, deleting, and verifying timestamps."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = calendarMode,
                    onClick = onSelectCalendar,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Calendar") },
                )
                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = !calendarMode,
                    onClick = onSelectList,
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = null) },
                    label = { Text("List") },
                )
            }

            HistoryDateBar(
                selectedLocalDate = selectedLocalDate,
                onPrevious = onPrevious,
                onNext = onNext,
                onPickDate = onPickDate,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = "$entriesCount entries",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }

                Button(
                    onClick = onAdd,
                    enabled = !displayLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = "Add for Date",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveDaySummaryCard(
    selectedLocalDate: LocalDate,
    entriesCount: Int,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = selectedLocalDate.toUiMonthDay(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${selectedLocalDate.toUiMonthYear()} · Daily archive",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = "$entriesCount entries",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun ArchiveCalendarCard(
    selectedLocalDate: LocalDate,
    monthCounts: Map<Int, Int>,
    onShiftMonth: (Int) -> Unit,
    onPickDay: (LocalDate) -> Unit,
) {
    val calendarCellWidth = 42.dp
    val monthStart = LocalDate(selectedLocalDate.year, selectedLocalDate.monthNumber, 1)
    val nextMonthStart = monthStart.plus(DatePeriod(months = 1))
    val daysInMonth = nextMonthStart.plus(DatePeriod(days = -1)).dayOfMonth
    val leadingEmptySlots = monthStart.dayOfWeek.isoDayNumber - 1
    val maxCount = monthCounts.values.maxOrNull() ?: 0

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = selectedLocalDate.toUiMonthYear(),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "Daily average ${monthCounts.averageOrZero().formatOneDecimal()} units",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Tap a day to pivot the archive and open that date in list mode.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row {
                    IconButton(onClick = { onShiftMonth(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    IconButton(onClick = { onShiftMonth(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { label ->
                    CalendarCell(width = calendarCellWidth) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            val totalSlots = leadingEmptySlots + daysInMonth
            val rows = (totalSlots + 6) / 7
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    for (column in 0 until 7) {
                        val slot = row * 7 + column
                        val day = slot - leadingEmptySlots + 1
                        if (day !in 1..daysInMonth) {
                            CalendarCell(width = calendarCellWidth) {}
                        } else {
                            val count = monthCounts[day] ?: 0
                            val date = LocalDate(selectedLocalDate.year, selectedLocalDate.monthNumber, day)
                            val background = when {
                                date == selectedLocalDate -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                count == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                maxCount <= 1 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
                                count >= maxCount -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                            }

                            Column(
                                modifier = Modifier
                                    .width(calendarCellWidth)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(background)
                                    .clickable { onPickDay(date) }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(text = day.toString(), style = MaterialTheme.typography.labelLarge)
                                Text(
                                    text = if (count > 0) count.toString() else "",
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

@Composable
private fun CalendarCell(
    width: androidx.compose.ui.unit.Dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun TrendCard(
    trendValue: Int,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Trend",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                )
                Text(
                    text = "${if (trendValue > 0) "+" else ""}$trendValue%",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "Reduction vs last month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = "↘",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

private fun Map<Int, Int>.averageOrZero(): Double = values.takeIf { it.isNotEmpty() }?.average() ?: 0.0

private fun monthTrendPercent(monthCounts: Map<Int, Int>): Int {
    if (monthCounts.isEmpty()) return 0
    val midpoint = monthCounts.keys.maxOrNull()?.div(2)?.coerceAtLeast(1) ?: return 0
    val firstHalf = monthCounts.filterKeys { it <= midpoint }.values.sum()
    val secondHalf = monthCounts.filterKeys { it > midpoint }.values.sum()
    if (firstHalf == 0) return 0
    return (((secondHalf - firstHalf).toDouble() / firstHalf.toDouble()) * 100).toInt()
}

private fun Double.formatOneDecimal(): String {
    val rounded = (this * 10).toInt() / 10.0
    val whole = rounded.toInt()
    val decimal = ((rounded - whole) * 10).toInt()
    return "$whole.$decimal"
}

private fun Instant.plusDays(days: Int, timeZone: TimeZone): Instant =
    plus(days, DateTimeUnit.DAY, timeZone)

private fun Instant.minusDays(days: Int, timeZone: TimeZone): Instant =
    plus(-days, DateTimeUnit.DAY, timeZone)

private fun LocalDate.toUiMonthYear(): String {
    val monthName = month.name.lowercase().replaceFirstChar { it.titlecase() }
    return "$monthName $year"
}

private fun LocalDate.toUiMonthDay(): String {
    val monthName = month.name.lowercase().replaceFirstChar { it.titlecase() }
    return "$monthName $dayOfMonth"
}
