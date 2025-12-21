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
import com.feragusper.smokeanalytics.libraries.smokes.presentation.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Presents a date picker dialog to the user, allowing for the selection of a date.
 *
 * This composable is Android-only, but works with [Instant] to stay consistent
 * with domain and data layers.
 *
 * @param initialDate The initial date shown in the picker.
 * @param onConfirm Callback invoked with the selected [Instant].
 * @param onDismiss Callback invoked when the dialog is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: Instant,
    onConfirm: (Instant) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochMilliseconds(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                datePickerState.selectedDateMillis
                    ?.let { Instant.fromEpochMilliseconds(it) }
                    ?.let(onConfirm)
            }) {
                Text(text = stringResource(id = R.string.smokes_date_time_picker_button_ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.smokes_date_time_picker_button_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}