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
) {
    Button(
        attrs = {
            classes(SmokeWebStyles.button, SmokeWebStyles.buttonPrimary)
            if (!enabled) attr("disabled", "true")
            onClick { if (enabled) onClick() }
        }
    ) { Text(text) }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        attrs = {
            classes(SmokeWebStyles.button)
            if (!enabled) attr("disabled", "true")
            onClick { if (enabled) onClick() }
        }
    ) { Text(text) }
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
                GhostButton(text = "Delete", onClick = onDelete)
            }
        }
    }
}