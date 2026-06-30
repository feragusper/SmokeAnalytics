package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.TriggerOption
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.normalizedTag
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

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
    availableTriggers: List<TriggerOption>,
    onSave: (tags: Set<String>) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
) {
    val options = remember(availableTriggers) {
        if (availableTriggers.isEmpty()) SmokeTrigger.defaultOptions() else availableTriggers
    }
    var selectedKeys by remember { mutableStateOf(emptySet<String>()) }
    var draft by remember { mutableStateOf("") }

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
                options.forEach { option ->
                    val isSelected = option.key in selectedKeys
                    Button(
                        attrs = {
                            onClick {
                                selectedKeys = if (isSelected) selectedKeys - option.key else selectedKeys + option.key
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
                    ) { Text(option.label) }
                }
            }

            Input(
                type = InputType.Text,
                attrs = {
                    value(draft)
                    onInput { draft = it.value }
                    attr("placeholder", "Add a tag…")
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
                        onClick {
                            val tags = selectedKeys + listOfNotNull(draft.normalizedTag())
                            if (tags.isNotEmpty()) onSave(tags)
                        }
                    }
                ) { Text("Save") }
            }
        }
    }
}
