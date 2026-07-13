package com.feragusper.smokeanalytics.libraries.smokes.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.feragusper.smokeanalytics.libraries.smokes.presentation.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@Composable
fun SwipeToDismissRow(
    itemKey: String,
    date: Instant,
    timeElapsedSincePreviousSmoke: Pair<Long, Long>,
    onDelete: () -> Unit,
    fullDateTimeEdit: Boolean,
    onEdit: (Instant) -> Unit,
    isPending: Boolean = false,
    pendingLabel: String? = null,
    use24HourClock: Boolean = true,
) {
    val timeZone = TimeZone.currentSystemDefault()
    val shape = RoundedCornerShape(22.dp)
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        onClick = { if (!isPending) showDatePicker = true },
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmokeItem(
                    modifier = Modifier.weight(1f),
                    date = date,
                    timeZone = timeZone,
                    timeAfterPrevious = timeElapsedSincePreviousSmoke,
                    use24HourClock = use24HourClock,
                    tone = elapsedToneFrom(
                        timeElapsedSincePreviousSmoke.first,
                        timeElapsedSincePreviousSmoke.second,
                    ),
                )
                IconButton(onClick = onDelete, enabled = !isPending) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            if (isPending) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
                    Text(
                        text = pendingLabel ?: "Updating…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (showDatePicker && !isPending) {
        var selectedDateTime: LocalDateTime = date.toLocalDateTime(timeZone)

        if (fullDateTimeEdit) {
            DateTimePickerDialog(
                initialDateTime = date,
                onDismiss = { showDatePicker = false },
                onDateSelected = { selectedInstant ->
                    selectedDateTime = selectedInstant.toLocalDateTime(timeZone)
                },
                onTimeSelected = { hour, minutes ->
                    showDatePicker = false
                    val updatedLocalDateTime = LocalDateTime(
                        year = selectedDateTime.year,
                        monthNumber = selectedDateTime.monthNumber,
                        dayOfMonth = selectedDateTime.dayOfMonth,
                        hour = hour,
                        minute = minutes,
                        second = selectedDateTime.second,
                        nanosecond = selectedDateTime.nanosecond,
                    )
                    onEdit(updatedLocalDateTime.toInstant(timeZone))
                }
            )
        } else {
            TimePickerDialog(
                initialDate = date,
                onConfirm = { hour, minutes ->
                    showDatePicker = false
                    val base = selectedDateTime
                    val updatedLocalDateTime = LocalDateTime(
                        year = base.year,
                        monthNumber = base.monthNumber,
                        dayOfMonth = base.dayOfMonth,
                        hour = hour,
                        minute = minutes,
                        second = base.second,
                        nanosecond = base.nanosecond,
                    )
                    onEdit(updatedLocalDateTime.toInstant(timeZone))
                },
                onDismiss = { showDatePicker = false },
            )
        }
    }
}

@Composable
private fun SmokeItem(
    modifier: Modifier = Modifier,
    date: Instant,
    timeZone: TimeZone,
    timeAfterPrevious: Pair<Long, Long>,
    use24HourClock: Boolean,
    tone: ElapsedTone,
) {
    val local = date.toLocalDateTime(timeZone)

    Row(
        modifier = modifier
            .background(color = tone.rowContainerColor(), shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp)
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = formatRowClock(local.hour, local.minute, use24HourClock),
                style = MaterialTheme.typography.bodyLarge,
                color = tone.rowContentColor()
            )

            val (hours, minutes) = timeAfterPrevious
            Text(
                text = "${stringResource(id = R.string.smokes_smoked_after)} ${
                    listOfNotNull(
                        pluralStringResource(
                            id = R.plurals.smokes_smoked_after_hours,
                            hours.toInt(),
                            hours.toInt()
                        ).takeIf { hours > 0 },
                        pluralStringResource(
                            id = R.plurals.smokes_smoked_after_minutes,
                            minutes.toInt(),
                            minutes.toInt()
                        )
                    ).joinToString(" and ")
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = tone.rowContentColor()
            )
        }
    }
}

@Composable
private fun ElapsedTone.rowContainerColor(): Color = when (this) {
    ElapsedTone.Urgent -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.48f)
    ElapsedTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.42f)
    ElapsedTone.Caution -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.34f)
    ElapsedTone.Calm -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)
}

@Composable
private fun ElapsedTone.rowContentColor(): Color = when (this) {
    ElapsedTone.Urgent -> MaterialTheme.colorScheme.onErrorContainer
    ElapsedTone.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
    ElapsedTone.Caution -> MaterialTheme.colorScheme.onSecondaryContainer
    ElapsedTone.Calm -> MaterialTheme.colorScheme.onPrimaryContainer
}

private enum class ElapsedTone {
    Urgent,
    Warning,
    Caution,
    Calm,
}

private fun elapsedToneFrom(hours: Long, minutes: Long): ElapsedTone {
    val totalMinutes = (hours * 60L + minutes).toInt()
    return when {
        totalMinutes >= 180 -> ElapsedTone.Calm
        totalMinutes >= 90 -> ElapsedTone.Caution
        totalMinutes >= 45 -> ElapsedTone.Warning
        else -> ElapsedTone.Urgent
    }
}

/**
 * Displays a dialog allowing the user to select both a date and a time, with the selections made in two steps.
 */
@Composable
fun DateTimePickerDialog(
    initialDateTime: Instant,
    onDismiss: () -> Unit,
    onDateSelected: (Instant) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
) {
    var dateTimeDialogType by remember { mutableStateOf(DateTimeDialogType.Date) }

    when (dateTimeDialogType) {
        DateTimeDialogType.Date -> {
            DatePickerDialog(
                initialDate = initialDateTime,
                onConfirm = {
                    onDateSelected(it)
                    dateTimeDialogType = DateTimeDialogType.Time
                },
                onDismiss = onDismiss,
            )
        }

        DateTimeDialogType.Time -> {
            TimePickerDialog(
                initialDate = initialDateTime,
                onConfirm = { hour, minute -> onTimeSelected(hour, minute) },
                onDismiss = onDismiss,
            )
        }
    }
}

/**
 * Displays a dialog for time selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialDate: Instant,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val local = initialDate.toLocalDateTime(TimeZone.currentSystemDefault())

    val timePickerState = rememberTimePickerState(
        initialHour = local.hour,
        initialMinute = local.minute,
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = stringResource(id = R.string.smokes_date_time_picker_title),
                    style = MaterialTheme.typography.labelMedium
                )

                TimePicker(state = timePickerState)

                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = onDismiss) {
                        Text(text = stringResource(id = R.string.smokes_date_time_picker_button_cancel))
                    }
                    Button(onClick = {
                        onConfirm(timePickerState.hour, timePickerState.minute)
                    }) {
                        Text(text = stringResource(id = R.string.smokes_date_time_picker_button_ok))
                    }
                }
            }
        }
    }
}

private enum class DateTimeDialogType {
    Date,
    Time
}

private fun formatRowClock(hour: Int, minute: Int, use24HourClock: Boolean): String {
    if (use24HourClock) return "%02d:%02d".format(hour, minute)
    val suffix = if (hour < 12) "AM" else "PM"
    val h12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "%d:%02d %s".format(h12, minute, suffix)
}
