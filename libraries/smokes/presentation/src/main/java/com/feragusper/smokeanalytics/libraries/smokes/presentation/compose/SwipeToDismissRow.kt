package com.feragusper.smokeanalytics.libraries.smokes.presentation.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeFormatted
import com.feragusper.smokeanalytics.libraries.smokes.presentation.R
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.roundToInt

/**
 * A row item for displaying smoke event details with swipe-to-dismiss functionality.
 * It allows users to delete the event by swiping and edit the event's time by tapping.
 *
 * @param date The date and time of the smoke event.
 * @param timeElapsedSincePreviousSmoke The time elapsed since the previous smoke event.
 * @param onDelete Callback invoked when the item is swiped to dismiss, indicating deletion.
 * @param fullDateTimeEdit Flag indicating if both date and time can be edited. If false, only time is editable.
 * @param onEdit Callback invoked with a new LocalDateTime when the user edits the event's time (or date and time).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissRow(
    date: LocalDateTime,
    timeElapsedSincePreviousSmoke: Pair<Long, Long>,
    onDelete: () -> Unit,
    fullDateTimeEdit: Boolean,
    onEdit: (LocalDateTime) -> Unit,
) = BoxWithConstraints {

    var willDismissDirection: SwipeToDismissBoxValue? by remember {
        mutableStateOf(null)
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (willDismissDirection == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    val width = constraints.maxWidth.toFloat()
    val threshold = 0.4f

    LaunchedEffect(key1 = Unit, block = {
        snapshotFlow { dismissState.requireOffset() }
            .collect {
                willDismissDirection = when {
                    it < -width * threshold -> SwipeToDismissBoxValue.EndToStart
                    else -> null
                }
            }
    })

    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(key1 = willDismissDirection, block = {
        if (willDismissDirection != null) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    })

    SwipeToDismissBox(state = dismissState,
        backgroundContent =
        {
            AnimatedContent(
                targetState = willDismissDirection != null,
                transitionSpec = {
                    fadeIn(
                        tween(0),
                        initialAlpha = if (targetState) 1f else 0f,
                    ) togetherWith fadeOut(
                        tween(0),
                        targetAlpha = if (targetState) .7f else 0f,
                    )
                }, label = ""
            ) { willDismiss ->
                val revealSize = remember { Animatable(if (willDismiss) 0f else 1f) }
                val iconSize = remember { Animatable(if (willDismiss) .8f else 1f) }
                LaunchedEffect(key1 = Unit, block = {
                    if (willDismiss) {
                        revealSize.snapTo(0f)
                        launch {
                            revealSize.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(400)
                            )
                        }
                        iconSize.snapTo(.8f)
                        iconSize.animateTo(
                            targetValue = 1.45f,
                            animationSpec = spring(Spring.DampingRatioHighBouncy)
                        )
                        iconSize.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(Spring.DampingRatioLowBouncy)
                        )
                    }
                })
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                        .background(color = Color.Red)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .scale(iconSize.value)
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = (10 * (1f - iconSize.value)).roundToInt()
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberVectorPainter(Icons.Default.Delete),
                            colorFilter = ColorFilter.tint(Color.Black),
                            contentDescription = null
                        )
                    }
                }
            }
        },
        enableDismissFromStartToEnd = false,
        content =
        {
            SmokeItem(
                date = date,
                timeAfterPrevious = timeElapsedSincePreviousSmoke,
                fullDateTimeEdit = fullDateTimeEdit,
                onEdit = onEdit
            )
        })
}

@Composable
private fun SmokeItem(
    date: LocalDateTime,
    timeAfterPrevious: Pair<Long, Long>,
    fullDateTimeEdit: Boolean,
    onEdit: (LocalDateTime) -> Unit,
) {
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
                text = date.timeFormatted(),
                style = MaterialTheme.typography.bodyLarge,
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
            )
        }

        var showDatePicker by remember {
            mutableStateOf(false)
        }

        IconButton(
            modifier = Modifier.wrapContentWidth(),
            onClick = { showDatePicker = true }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_pencil),
                contentDescription = null
            )
        }

        if (showDatePicker) {
            var selectedDateTime = date

            if (fullDateTimeEdit) {
                DateTimePickerDialog(
                    initialDateTime = date,
                    onDismiss = {
                        showDatePicker = false
                    },
                    onDateSelected = { date ->
                        selectedDateTime = date
                    },
                    onTimeSelected = { hour, minutes ->
                        showDatePicker = false
                        onEdit(selectedDateTime.toLocalDate().atTime(hour, minutes))
                    }
                )
            } else {
                TimePickerDialog(
                    initialDate = date,
                    onConfirm = { hour, minutes ->
                        showDatePicker = false
                        onEdit(selectedDateTime.toLocalDate().atTime(hour, minutes))
                    },
                    onDismiss = {
                        showDatePicker = false
                    },
                )
            }

        }
    }

}

private enum class DateTimeDialogType {
    Date,
    Time
}

/**
 * Displays a dialog allowing the user to select both a date and a time, with the selections made in two steps.
 * First, the date is chosen, followed by the time.
 *
 * @param initialDateTime The initial date and time to display in the picker.
 * @param onDismiss Callback invoked when the dialog is dismissed without a selection.
 * @param onDateSelected Callback invoked when a date is selected, before the time selection step.
 * @param onTimeSelected Callback invoked with the selected hour and minute after both date and time have been chosen.
 */
@Composable
private fun DateTimePickerDialog(
    initialDateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDateTime) -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
) {

    var dateTimeDialogType by remember {
        mutableStateOf(DateTimeDialogType.Date)
    }

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
                onConfirm = { hour, minute ->
                    onTimeSelected(hour, minute)
                },
                onDismiss = onDismiss,
            )
        }
    }

}

/**
 * Displays a dialog for time selection, providing an interface for choosing an hour and minute.
 *
 * @param initialDate The initial date and time to display in the picker.
 * @param onDismiss Callback invoked when the dialog is dismissed without making a selection.
 * @param onConfirm Callback invoked with the selected hour and minute upon confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialDate: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialDate.hour,
        initialMinute = initialDate.minute,
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
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
                TimePicker(
                    state = timePickerState
                )
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        onDismiss()
                    }) {
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