package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

/** Human-readable label for a trigger chip. */
internal fun SmokeTrigger.label(): String = when (this) {
    SmokeTrigger.COFFEE -> "Coffee"
    SmokeTrigger.ALCOHOL -> "Alcohol"
    SmokeTrigger.BOREDOM -> "Boredom"
    SmokeTrigger.ANXIETY -> "Anxiety"
    SmokeTrigger.STRESS -> "Stress"
    SmokeTrigger.AFTER_MEAL -> "After a meal"
    SmokeTrigger.SOCIAL -> "Social"
    SmokeTrigger.BREAK -> "Break"
    SmokeTrigger.DRIVING -> "Driving"
    SmokeTrigger.PHONE -> "Phone"
}

/**
 * Home card reminding the user that some smokes (logged from the watch, or whose prompt
 * was dismissed) still have no trigger. Tapping the CTA opens the prompt for the next one.
 */
@Composable
internal fun RelationshipReminderCardWeb(
    pendingCount: Int,
    onAdd: () -> Unit,
) {
    Div(attrs = { classes(SmokeWebStyles.card) }) {
        H3 { Text("What were these about?") }
        P {
            Text(
                if (pendingCount == 1) {
                    "1 cigarette still needs a trigger"
                } else {
                    "$pendingCount cigarettes still need a trigger"
                }
            )
        }
        Button(attrs = { onClick { onAdd() } }) { Text("Add trigger") }
    }
}

/**
 * Modal asking what a smoke was related to: multi-select chips plus an "Other" free-text
 * field. The user can save, declare "no relation", or dismiss (the smoke stays untracked).
 */
@Composable
internal fun RelationshipPromptDialogWeb(
    onSave: (triggers: Set<SmokeTrigger>, note: String?) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(emptySet<SmokeTrigger>()) }
    var note by remember { mutableStateOf("") }

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
            onClick { onDismiss() }
        }
    ) {
        Div(
            attrs = {
                style {
                    property("background", "white")
                    property("padding", "20px")
                    property("border-radius", "12px")
                    property("min-width", "320px")
                    property("max-width", "90vw")
                }
                onClick { it.stopPropagation() }
            }
        ) {
            H3 { Text("What was it related to?") }
            P { Text("Tag what triggered this cigarette, or skip if it was nothing in particular.") }

            Div(
                attrs = {
                    style {
                        property("display", "flex")
                        property("flex-wrap", "wrap")
                        property("gap", "8px")
                        property("margin", "12px 0")
                    }
                }
            ) {
                SmokeTrigger.entries.forEach { trigger ->
                    val isSelected = trigger in selected
                    Button(
                        attrs = {
                            onClick {
                                selected = if (isSelected) selected - trigger else selected + trigger
                            }
                            style {
                                property("border-radius", "999px")
                                property("padding", "6px 14px")
                                property("cursor", "pointer")
                                property("border", "1px solid #006A6A")
                                if (isSelected) {
                                    property("background", "#006A6A")
                                    property("color", "white")
                                } else {
                                    property("background", "transparent")
                                    property("color", "#006A6A")
                                }
                            }
                        }
                    ) { Text(trigger.label()) }
                }
            }

            Input(
                type = InputType.Text,
                attrs = {
                    value(note)
                    onInput { note = it.value }
                    attr("placeholder", "Other (add your own…)")
                    style {
                        property("width", "100%")
                        property("box-sizing", "border-box")
                        property("padding", "8px")
                    }
                }
            )

            Div(
                attrs = {
                    style {
                        property("display", "flex")
                        property("gap", "8px")
                        property("margin-top", "16px")
                        property("justify-content", "flex-end")
                    }
                }
            ) {
                Button(attrs = { onClick { onSkip() } }) { Text("No relation") }
                Button(
                    attrs = {
                        val enabled = selected.isNotEmpty() || note.isNotBlank()
                        if (!enabled) disabled()
                        onClick {
                            onSave(selected, note.trim().takeIf { it.isNotEmpty() })
                        }
                    }
                ) { Text("Save") }
            }
        }
    }
}
