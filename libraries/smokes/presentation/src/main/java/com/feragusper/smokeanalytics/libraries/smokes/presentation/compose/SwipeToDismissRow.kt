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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import de.charlex.compose.RevealDirection
import de.charlex.compose.RevealSwipe
import de.charlex.compose.rememberRevealState
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * A row item for displaying smoke event details with swipe-to-dismiss functionality.
 * It allows users to delete the event by swiping and edit the event's time by tapping.
 *
 * All time values are represented as [Instant] to stay consistent with domain/data and Web.
 *
 * @param date The date/time of the smoke event.
 * @param timeElapsedSincePreviousSmoke The time elapsed since the previous smoke event.
 * @param onDelete Callback invoked when the item is deleted.
 * @param fullDateTimeEdit Flag indicating if both date and time can be edited. If false, only time is editable.
 * @param onEdit Callback invoked with a new [Instant] when the user edits the event.
 */
@Composable
fun SwipeToDismissRow(
    date: Instant,
    timeElapsedSincePreviousSmoke: Pair<Long, Long>,
    onDelete: () -> Unit,
    fullDateTimeEdit: Boolean,
    onEdit: (Instant) -> Unit,
) {
    val timeZone = TimeZone.currentSystemDefault()
    val shape = MaterialTheme.shapes.medium

    val state = rememberRevealState(
        directions = setOf(RevealDirection.StartToEnd, RevealDirection.EndToStart),
    )

    var showDatePicker by remember { mutableStateOf(false) }

    RevealSwipe(
        modifier = Modifier.padding(vertical = 5.dp),
        state = state,
        shape = shape,
        backgroundCardStartColor = MaterialTheme.colorScheme.primaryContainer,
        backgroundCardEndColor = MaterialTheme.colorScheme.errorContainer,
        backgroundStartActionLabel = "Edit smoke",
        backgroundEndActionLabel = "Delete smoke",
        onBackgroundStartClick = {
            showDatePicker = true
            true
        },
        onBackgroundEndClick = {
            onDelete()
            true
        },
        hiddenContentStart = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_pencil),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        hiddenContentEnd = {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        card = { cardShape, content ->
            Card(
                modifier = Modifier.matchParentSize(),
                colors = CardDefaults.cardColors(
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = Color.Transparent
                ),
                shape = cardShape,
                content = content
            )
        }
    ) {
        SmokeItem(
            date = date,
            timeZone = timeZone,
            timeAfterPrevious = timeElapsedSincePreviousSmoke,
        )
    }

    if (showDatePicker) {
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
    date: Instant,
    timeZone: TimeZone,
    timeAfterPrevious: Pair<Long, Long>,
) {
    val local = date.toLocalDateTime(timeZone)

    Row(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "%02d:%02d".format(local.hour, local.minute),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
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
                color = MaterialTheme.colorScheme.onBackground
            )
        }
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