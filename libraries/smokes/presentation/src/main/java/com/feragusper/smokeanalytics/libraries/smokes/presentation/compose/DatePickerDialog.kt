package com.feragusper.smokeanalytics.libraries.smokes.presentation.compose

import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.utcMillis
import com.feragusper.smokeanalytics.libraries.smokes.presentation.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Presents a date picker dialog to the user, allowing for the selection of a date from a calendar-style UI.
 * It's built using Material 3 components and provides customization for initial date selection and date selection constraints.
 *
 * @param initialDate The initial date to be shown when the date picker is first displayed.
 * @param onConfirm Callback function invoked with the selected [LocalDateTime] when the user confirms their choice.
 * @param onDismiss Callback function invoked when the date picker dialog is dismissed without a date selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: LocalDateTime,
    onConfirm: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    // Remembering the state of the DatePicker, initializing with the passed `initialDate`.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.utcMillis(), // Convert initialDate to milliseconds.
        selectableDates = object : SelectableDates {
            // Custom constraint to limit selectable dates to the current and past dates only.
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    // Building the DatePickerDialog with specified buttons and actions.
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                // Handling date confirmation, converting the selected milliseconds back to LocalDateTime and invoking `onConfirm`.
                datePickerState.selectedDateMillis?.let { millis ->
                    Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                }?.let(onConfirm)
            }) {
                Text(text = stringResource(id = R.string.smokes_date_time_picker_button_ok))
            }
        },
        dismissButton = {
            // Handling dialog dismissal.
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.smokes_date_time_picker_button_cancel))
            }
        }
    ) {
        // The actual DatePicker composable with the remembered state.
        DatePicker(state = datePickerState)
    }
}
