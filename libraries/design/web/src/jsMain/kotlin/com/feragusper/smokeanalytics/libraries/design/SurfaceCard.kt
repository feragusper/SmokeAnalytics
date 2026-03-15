package com.feragusper.smokeanalytics.libraries.design

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun SurfaceCard(
    vararg extraClasses: String,
    content: @Composable () -> Unit,
) {
    Div(
        attrs = {
            classes(SmokeWebStyles.card)
            if (extraClasses.isNotEmpty()) {
                classes(*extraClasses)
            }
        }
    ) {
        content()
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    SurfaceCard(SmokeWebStyles.statCard) {
        Div(
            attrs = {
                if (onClick != null) {
                    onClick { onClick() }
                }
            }
        ) {
            Div(attrs = { classes(SmokeWebStyles.statTitle) }) { Text(title) }
            Div(attrs = { classes(SmokeWebStyles.statValue) }) { Text(value) }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    extraClass: String? = null,
) {
    ActionButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        extraClass = listOfNotNull(SmokeWebStyles.buttonPrimary, extraClass).joinToString(" "),
    )
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    ActionButton(text = text, onClick = onClick, enabled = enabled)
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    ActionButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        extraClass = SmokeWebStyles.buttonDanger,
    )
}

@Composable
fun SmokeRow(
    time: String,
    subtitle: String,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    Div(attrs = { classes(SmokeWebStyles.listRow) }) {
        Div {
            Div(attrs = { classes(SmokeWebStyles.timeText) }) { Text(time) }
            Div(attrs = { classes(SmokeWebStyles.subText) }) { Text(subtitle) }
        }
        Div {
            if (onEdit != null) {
                GhostButton(text = "Edit", onClick = onEdit)
                Span { Text(" ") }
            }
            if (onDelete != null) {
                DangerButton(text = "Delete", onClick = onDelete)
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    extraClass: String? = null,
) {
    Button(
        attrs = {
            classes(SmokeWebStyles.button)
            extraClass?.split(" ")?.filter { it.isNotBlank() }?.let { classes(*it.toTypedArray()) }
            if (!enabled) {
                attr("disabled", "true")
                classes(SmokeWebStyles.buttonDisabled)
            }
            onClick { if (enabled) onClick() }
        }
    ) { Text(text) }
}
