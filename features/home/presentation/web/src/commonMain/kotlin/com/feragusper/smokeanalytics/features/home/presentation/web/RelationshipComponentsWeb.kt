package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.TriggerOption
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.normalizedTag
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

/** A pending (untracked) smoke shown in the reminder card: its id and a human date/time label. */
internal data class PendingTriggerSmoke(
    val id: String,
    val label: String,
)

private const val MAX_PENDING_SHOWN = 8

/**
 * Home card reminding the user that some smokes (logged from the watch, or whose prompt was
 * dismissed) still have no trigger. Each one carries its own date/time so the user can recall
 * what it was about and tag them one at a time — not all at once.
 */
@Composable
internal fun RelationshipReminderCardWeb(
    pending: List<PendingTriggerSmoke>,
    onOpen: (smokeId: String) -> Unit,
) {
    val shown = pending.take(MAX_PENDING_SHOWN)
    Div(attrs = { classes(SmokeWebStyles.card) }) {
        H3 { Text("What were these about?") }
        P {
            Text(
                if (pending.size == 1) {
                    "1 cigarette still needs a trigger — tag it below."
                } else {
                    "${pending.size} cigarettes still need a trigger — tag them one at a time."
                }
            )
        }
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:8px;margin-top:8px;") }) {
            shown.forEach { item ->
                Div(attrs = {
                    attr(
                        "style",
                        "display:flex;align-items:center;justify-content:space-between;gap:12px;" +
                            "padding:8px 0;border-top:1px solid var(--sa-color-outline);",
                    )
                }) {
                    Span(attrs = { attr("style", "font-size:14px;") }) { Text(item.label) }
                    GhostButton(text = "Add trigger", onClick = { onOpen(item.id) })
                }
            }
        }
        if (pending.size > shown.size) {
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text("+${pending.size - shown.size} more")
            }
        }
    }
}

/**
 * Modal asking what a single smoke was related to. Shows which cigarette is being tagged
 * (its date/time), multi-select chips, and an "Add a tag" field. Save / No relation / dismiss.
 */
@Composable
internal fun RelationshipPromptDialogWeb(
    availableTriggers: List<TriggerOption>?,
    dateLabel: String?,
    onSave: (tags: Set<String>) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Freeze the catalog the moment it's loaded (keyed on loaded-ness, not the list) so
    // chips don't pop in mid-dialog when a background refresh lands; while null the dialog
    // shows a loading row instead of a partial default list.
    val options = remember(availableTriggers != null) { availableTriggers }
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
                    property("background", "var(--sa-color-surface)")
                    property("color", "var(--sa-color-onSurface)")
                    property("padding", "20px")
                    property("border-radius", "12px")
                    property("min-width", "320px")
                    property("max-width", "90vw")
                    property("border", "1px solid var(--sa-color-outline)")
                }
                onClick { it.stopPropagation() }
            }
        ) {
            H3 { Text("What was it related to?") }
            dateLabel?.let {
                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text("Logged $it") }
            }
            P { Text("Tag what triggered this cigarette, or skip if it was nothing in particular.") }

            if (options == null) {
                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text("Loading your tags…") }
            } else Div(
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
                                property("border", "1px solid var(--sa-color-primary)")
                                if (isSelected) {
                                    property("background", "var(--sa-color-primary)")
                                    property("color", "var(--sa-color-onPrimary)")
                                } else {
                                    property("background", "transparent")
                                    property("color", "var(--sa-color-primary)")
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
                        property("background", "var(--sa-color-surface-strong)")
                        property("color", "var(--sa-color-onSurface)")
                        property("border", "1px solid var(--sa-color-outline)")
                        property("border-radius", "8px")
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
                GhostButton(text = "No relation", onClick = onSkip)
                PrimaryButton(
                    text = "Save",
                    enabled = selectedKeys.isNotEmpty() || draft.normalizedTag() != null,
                    onClick = {
                        val tags = selectedKeys + listOfNotNull(draft.normalizedTag())
                        if (tags.isNotEmpty()) onSave(tags)
                    },
                )
            }
        }
    }
}
