package com.feragusper.smokeanalytics.libraries.design

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

enum class StatusTone {
    Default,
    Busy,
    Error,
}

@Composable
fun PageSectionHeader(
    title: String,
    subtitle: String? = null,
    eyebrow: String? = null,
    badgeText: String? = null,
    badgeTone: StatusTone = StatusTone.Default,
    actions: (@Composable () -> Unit)? = null,
) {
    Div(attrs = { classes(SmokeWebStyles.pageHero) }) {
        Div(attrs = { classes(SmokeWebStyles.pageHeroText) }) {
            eyebrow?.let { label ->
                Div(attrs = { classes(SmokeWebStyles.pageHeroEyebrow) }) { Text(label) }
            }
            Div(attrs = { classes(SmokeWebStyles.pageHeroTitle) }) { Text(title) }
            subtitle?.let { text ->
                Div(attrs = { classes(SmokeWebStyles.pageHeroSubtitle) }) { Text(text) }
            }
        }

        Div(attrs = { classes(SmokeWebStyles.pageHeroActions) }) {
            badgeText?.let { text ->
                Div(
                    attrs = {
                        classes(SmokeWebStyles.statusPill)
                        when (badgeTone) {
                            StatusTone.Default -> Unit
                            StatusTone.Busy -> classes(SmokeWebStyles.statusPillBusy)
                            StatusTone.Error -> classes(SmokeWebStyles.statusPillError)
                        }
                    }
                ) { Text(text) }
            }
            actions?.invoke()
        }
    }
}

@Composable
fun LoadingSkeletonCard(
    heightPx: Int = 120,
    lineWidths: List<String> = listOf("38%", "72%", "54%"),
) {
    SurfaceCard {
        Div(
            attrs = {
                classes(SmokeWebStyles.skeletonBlock)
                attr("style", "height: 20px;")
            }
        )
        Div(
            attrs = {
                classes(SmokeWebStyles.skeletonBlock)
                attr("style", "height: ${heightPx - 44}px; margin-top: 16px;")
            }
        )
        lineWidths.forEach { widthValue ->
            Div(
                attrs = {
                    classes(SmokeWebStyles.skeletonBlock)
                    attr("style", "height: 12px; margin-top: 12px; width: $widthValue;")
                }
            )
        }
    }
}

@Composable
fun LoadingSkeletonList(
    rows: Int = 4,
) {
    Div(attrs = { classes(SmokeWebStyles.list) }) {
        repeat(rows) {
            SurfaceCard {
                Div(
                    attrs = {
                        classes(SmokeWebStyles.skeletonBlock)
                        attr("style", "height: 16px; width: 28%;")
                    }
                )
                Div(
                    attrs = {
                        classes(SmokeWebStyles.skeletonBlock)
                        attr("style", "height: 12px; margin-top: 12px; width: 56%;")
                    }
                )
                Div(
                    attrs = {
                        classes(SmokeWebStyles.skeletonBlock)
                        attr("style", "height: 38px; margin-top: 18px;")
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    SurfaceCard {
        Div(attrs = { classes(SmokeWebStyles.emptyState) }) {
            Div(attrs = { classes(SmokeWebStyles.emptyStateTitle) }) { Text(title) }
            Div(attrs = { classes(SmokeWebStyles.emptyStateBody) }) { Text(message) }
            if (actionLabel != null && onAction != null) {
                GhostButton(text = actionLabel, onClick = onAction)
            }
        }
    }
}

@Composable
fun InlineErrorCard(
    title: String = "Something went wrong",
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    SurfaceCard(SmokeWebStyles.inlineError) {
        Div(attrs = { classes(SmokeWebStyles.inlineErrorTitle) }) { Text(title) }
        Div(attrs = { classes(SmokeWebStyles.inlineErrorBody) }) { Text(message) }
        if (actionLabel != null && onAction != null) {
            GhostButton(text = actionLabel, onClick = onAction)
        }
    }
}
