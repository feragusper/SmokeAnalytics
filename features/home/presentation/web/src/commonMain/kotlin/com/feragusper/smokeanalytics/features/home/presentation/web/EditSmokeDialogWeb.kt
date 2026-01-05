package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text

/**
 * Displays a dialog for editing a smoke.
 *
 * @param initialInstant The initial instant to be displayed in the dialog.
 * @param fullDateTimeEdit Whether to allow full date and time editing.
 * @param onDismiss Callback to be invoked when the dialog is dismissed.
 * @param onConfirm Callback to be invoked when the user confirms the changes.
 */
@Composable
internal fun EditSmokeDialogWeb(
    initialInstant: Instant,
    fullDateTimeEdit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Instant) -> Unit,
) {
    val timeZone = remember { TimeZone.currentSystemDefault() }

    var dateValue by remember(initialInstant) {
        mutableStateOf(
            initialInstant.toDateInputValue(
                timeZone
            )
        )
    }
    var timeValue by remember(initialInstant) {
        mutableStateOf(
            initialInstant.toTimeInputValue(
                timeZone
            )
        )
    }

    Div(
        attrs = {
            style {
                property("position", "fixed")
                property("inset", "0")
                property("background", "rgba(0,0,0,0.4)")
                property("display", "flex")
                property("align-items", "center")
                property("justify-content", "center")
                property("z-index", "9999")
            }
            onClick { onDismiss() } // click outside closes
        }
    ) {
        Div(
            attrs = {
                style {
                    property("background", "white")
                    property("padding", "16px")
                    property("border-radius", "12px")
                    property("min-width", "320px")
                    property("max-width", "90vw")
                }
                onClick { it.stopPropagation() } // prevent backdrop click
            }
        ) {
            H3 { Text("Edit smoke") }

            if (fullDateTimeEdit) {
                Label(forId = "date") { Text("Date") }
                Input(
                    type = InputType.Date,
                    attrs = {
                        id("date")
                        value(dateValue)
                        onInput { dateValue = it.value }
                    }
                )
                Br()
                Br()
            }

            Label(forId = "time") { Text("Time") }
            Input(
                type = InputType.Time,
                attrs = {
                    id("time")
                    value(timeValue)
                    onInput { timeValue = it.value }
                }
            )

            Br()
            Br()

            Div {
                Button(attrs = { onClick { onDismiss() } }) { Text("Cancel") }
                Text(" ")
                Button(
                    attrs = {
                        onClick {
                            val baseDate = if (fullDateTimeEdit) {
                                dateValue
                            } else {
                                initialInstant.toDateInputValue(timeZone) // keep original day
                            }
                            onConfirm(dateTimeInputsToInstant(baseDate, timeValue, timeZone))
                        }
                    }
                ) { Text("OK") }
            }
        }
    }
}