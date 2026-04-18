package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.home.domain.ElapsedTone
import com.feragusper.smokeanalytics.features.home.domain.GapFocusSummary
import com.feragusper.smokeanalytics.features.home.domain.HomeHeroMetricIcon
import com.feragusper.smokeanalytics.features.home.domain.HomeHeroProgressTone
import com.feragusper.smokeanalytics.features.home.domain.gapFocusSummary
import com.feragusper.smokeanalytics.features.home.domain.homeHeroProgress
import com.feragusper.smokeanalytics.features.home.domain.homeHeroReadout
import com.feragusper.smokeanalytics.features.home.domain.homeGoalNarrative
import com.feragusper.smokeanalytics.features.home.domain.toElapsedGapLabel
import com.feragusper.smokeanalytics.features.home.domain.toHomeClockLabel
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.web.mvi.HomeWebStore
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.StatusTone
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun HomeWebScreen(
    store: HomeWebStore,
    onNavigateToHistory: () -> Unit,
    onNavigateToGoals: () -> Unit,
) {
    LaunchedEffect(store) { store.start() }

    val state by store.state.collectAsState()

    state.Render(
        onIntent = { intent ->
            when (intent) {
                HomeIntent.OnClickHistory -> onNavigateToHistory()
                HomeIntent.OnClickGoals -> onNavigateToGoals()
                else -> store.send(intent)
            }
        },
    )
}

@Composable
fun HomeViewState.Render(
    onIntent: (HomeIntent) -> Unit,
) {
    val narrative = homeGoalNarrative(
        goalProgress = goalProgress,
        smokesPerDay = smokesPerDay,
        timeSinceLastCigarette = timeSinceLastCigarette,
        awakeMinutesPerDay = awakeMinutesPerDay,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
    )
    val heroProgress = homeHeroProgress(
        goalProgress = goalProgress,
        smokesPerDay = smokesPerDay,
        timeSinceLastCigarette = timeSinceLastCigarette,
        awakeMinutesPerDay = awakeMinutesPerDay,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
    )
    val heroReadout = homeHeroReadout(
        goalProgress = goalProgress,
        smokesPerDay = smokesPerDay,
        timeSinceLastCigarette = timeSinceLastCigarette,
        awakeMinutesPerDay = awakeMinutesPerDay,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
    )
    val gapFocus = gapFocusSummary(
        elapsedMinutes = timeSinceLastCigarette?.let { it.first * 60 + it.second },
        rateSummary = rateSummary,
        goalProgress = goalProgress,
        smokesPerDay = smokesPerDay,
        awakeMinutesPerDay = awakeMinutesPerDay,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
    )
    val showingInitialSkeleton = error == null && (
        displayLoading ||
            (timeSinceLastCigarette == null && goalProgress == null && smokesPerDay == null)
    )

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = greetingTitle ?: "Home",
            eyebrow = "Home",
            badgeText = when {
                displayRefreshLoading -> "Refreshing"
                error != null -> "Needs attention"
                else -> null
            },
            badgeTone = when {
                displayRefreshLoading -> StatusTone.Busy
                error != null -> StatusTone.Error
                else -> StatusTone.Default
            },
        )

        greetingMessage?.let { message ->
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) { Text(message) }
        }

        if (error != null) {
            EmptyStateCard(
                title = if (error == HomeViewState.HomeError.NotLoggedIn) "Session required" else "Could not refresh home",
                message = when (error) {
                    HomeViewState.HomeError.NotLoggedIn ->
                        "Home needs an active session to line up your goal, last cigarette, and latest gap."
                    HomeViewState.HomeError.Generic ->
                        "The goal-first home could not be refreshed. Try again in a moment."
                },
                actionLabel = if (error == HomeViewState.HomeError.NotLoggedIn) "Open archive" else "Retry",
                onAction = if (error == HomeViewState.HomeError.NotLoggedIn) {
                    { onIntent(HomeIntent.OnClickHistory) }
                } else {
                    { onIntent(HomeIntent.RefreshFetchSmokes) }
                },
            )
        }

        if (showingInitialSkeleton) {
            LoadingSkeletonCard(heightPx = 260, lineWidths = listOf("34%", "74%", "60%"))
            LoadingSkeletonCard(heightPx = 180, lineWidths = listOf("28%", "40%", "52%"))
            LoadingSkeletonCard(heightPx = 180, lineWidths = listOf("26%", "52%", "62%"))
            LoadingSkeletonCard(heightPx = 140, lineWidths = listOf("22%", "58%"))
        } else {
            GoalHeroCard(
                narrative = narrative,
                heroProgress = heroProgress,
                heroReadout = heroReadout,
            )

            DetachedTrackAction(
                elapsedTone = elapsedTone,
                onAddSmoke = { onIntent(HomeIntent.AddSmoke) },
            )

            HomeInsightGrid(
                lastSmokeTimeLabel = lastSmoke?.date?.toHomeClockLabel(),
                timeSinceLastCigarette = timeSinceLastCigarette,
                gapFocus = gapFocus,
                consistencyLabel = narrative.consistencyLabel,
                statusLabel = narrative.statusLabel,
                hasActiveGoal = hasActiveGoal,
                elapsedTone = elapsedTone,
                onAddSmoke = { onIntent(HomeIntent.AddSmoke) },
                onOpenGoals = { onIntent(HomeIntent.OnClickGoals) },
            )

            if (canStartNewDay) {
                EveningResetCard(
                    onStartNewDay = { onIntent(HomeIntent.StartNewDay) },
                )
            }
        }
    }
}

@Composable
private fun GoalHeroCard(
    narrative: com.feragusper.smokeanalytics.features.home.domain.HomeGoalNarrative,
    heroProgress: com.feragusper.smokeanalytics.features.home.domain.HomeHeroProgress,
    heroReadout: com.feragusper.smokeanalytics.features.home.domain.HomeHeroReadout,
) {
    SurfaceCard {
        Div(
            attrs = {
                attr("style", "display:flex;flex-direction:column;gap:16px;")
            }
        ) {
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
                Div(attrs = { attr("style", "display:flex;justify-content:space-between;gap:14px;align-items:center;flex-wrap:wrap;") }) {
                    HomeSectionChip("⌁", "Goal", heroProgress.tone.accentColor())
                    TonePill(
                        text = narrative.statusLabel,
                        background = heroProgress.tone.pillBackground(),
                        foreground = heroProgress.tone.pillForeground(),
                    )
                }
                Div(attrs = { attr("style", "font-size:clamp(28px,4vw,40px);font-weight:850;line-height:1.02;color:var(--sa-color-on-surface);max-width:720px;") }) {
                    Text(narrative.heroTitle)
                }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) { Text(narrative.heroSupporting) }
            }
            GoalHeroReadoutCard(heroProgress = heroProgress, heroReadout = heroReadout)
        }
    }
}

@Composable
private fun GoalHeroReadoutCard(
    heroProgress: com.feragusper.smokeanalytics.features.home.domain.HomeHeroProgress,
    heroReadout: com.feragusper.smokeanalytics.features.home.domain.HomeHeroReadout,
) {
    Div(
        attrs = {
            attr(
                "style",
                "display:flex;flex-direction:column;gap:14px;padding:16px 18px;border-radius:24px;background:var(--sa-color-surface-container-low);"
            )
        }
    ) {
        heroReadout.meterLabel?.let { label ->
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:8px;") }) {
                Div(attrs = { attr("style", "display:flex;justify-content:space-between;gap:12px;align-items:baseline;flex-wrap:wrap;") }) {
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text(label)
                    }
                    heroReadout.meterValue?.let { value ->
                        Div(attrs = { attr("style", "font-size:18px;font-weight:800;color:${heroProgress.tone.accentColor()};") }) {
                            Text(value)
                        }
                    }
                }
                Div(attrs = { attr("style", "height:10px;border-radius:999px;background:rgba(20,32,34,0.08);overflow:hidden;") }) {
                    Div(
                        attrs = {
                            attr(
                                "style",
                                "width:${(((heroReadout.meterFraction ?: heroProgress.fraction).coerceIn(0f, 1f)) * 100)}%;height:100%;border-radius:999px;background:${heroProgress.tone.accentColor()};"
                            )
                        }
                    )
                }
            }
        }
        Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:12px;") }) {
            heroReadout.metrics.forEach { metric ->
                GoalHeroMetricCard(
                    label = metric.label,
                    value = metric.value,
                    supporting = metric.supporting,
                    icon = metric.icon,
                )
            }
        }
    }
}

@Composable
private fun GoalHeroMetricCard(
    label: String,
    value: String,
    supporting: String?,
    icon: HomeHeroMetricIcon,
) {
    Div(
        attrs = {
            attr(
                "style",
                "display:flex;flex-direction:column;gap:10px;padding:14px 16px;border-radius:20px;background:var(--sa-color-surface-strong);"
            )
        }
    ) {
        HomeSectionChip(metricGlyph(icon), label, "var(--sa-color-primary)")
        Div(attrs = { attr("style", "font-size:24px;font-weight:800;line-height:1.1;color:var(--sa-color-on-surface);") }) {
            Text(value)
        }
        supporting?.let {
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text(it)
            }
        }
    }
}

@Composable
private fun HomeInsightGrid(
    lastSmokeTimeLabel: String?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    gapFocus: GapFocusSummary,
    consistencyLabel: String,
    statusLabel: String,
    hasActiveGoal: Boolean,
    elapsedTone: ElapsedTone,
    onAddSmoke: () -> Unit,
    onOpenGoals: () -> Unit,
) {
    Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:14px;") }) {
        CurrentGapCard(
            lastSmokeTimeLabel = lastSmokeTimeLabel,
            timeSinceLastCigarette = timeSinceLastCigarette,
            gapFocus = gapFocus,
            elapsedTone = elapsedTone,
        )
        MomentumCard(
            consistencyLabel = consistencyLabel,
            statusLabel = statusLabel,
            hasActiveGoal = hasActiveGoal,
            onOpenGoals = onOpenGoals,
        )
    }
}

@Composable
private fun CurrentGapCard(
    lastSmokeTimeLabel: String?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    gapFocus: GapFocusSummary,
    elapsedTone: ElapsedTone,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            HomeSectionChip("◷", "Current gap", elapsedTone.pillForeground())
            Div(attrs = { attr("style", "font-size:38px;font-weight:850;line-height:1.0;color:var(--sa-color-on-surface);") }) {
                Text(timeSinceLastCigarette.toElapsedGapLabel())
            }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text("Since ${lastSmokeTimeLabel?.let { "$it hs" } ?: "--:--"}")
            }
            TonePill(
                text = gapFocus.pulseSummaryText,
                background = elapsedTone.pillBackground(),
                foreground = elapsedTone.pillForeground(),
                large = true,
            )
        }
    }
}

@Composable
private fun DetachedTrackAction(
    elapsedTone: ElapsedTone,
    onAddSmoke: () -> Unit,
) {
    Div(
        attrs = {
            attr("style", "display:flex;justify-content:flex-end;position:sticky;bottom:16px;z-index:4;pointer-events:none;")
        }
    ) {
        Div(attrs = { attr("style", "pointer-events:auto;") }) {
            PrimaryButton(
                text = "Track",
                onClick = onAddSmoke,
                extraClass = elapsedTone.buttonClass(),
            )
        }
    }
}

@Composable
private fun MomentumCard(
    consistencyLabel: String,
    statusLabel: String,
    hasActiveGoal: Boolean,
    onOpenGoals: () -> Unit,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            Div(attrs = { attr("style", "display:flex;justify-content:space-between;gap:12px;align-items:center;flex-wrap:wrap;") }) {
                HomeSectionChip("↗", "Rhythm", "var(--sa-color-primary)")
                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(statusLabel) }
            }
            Div(attrs = { attr("style", "font-size:28px;font-weight:800;line-height:1.1;color:var(--sa-color-on-surface);") }) {
                Text(consistencyLabel)
            }
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                QuietButton(
                    text = if (hasActiveGoal) "Open You" else "Set goal",
                    onClick = onOpenGoals,
                )
            }
        }
    }
}

@Composable
private fun EveningResetCard(
    onStartNewDay: () -> Unit,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            HomeSectionChip("◌", "Reset day", "var(--sa-color-primary)")
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                Text("If the day started earlier than usual, reset the reflection window now and keep Home aligned with the day you are actually living.")
            }
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                PrimaryButton(
                    text = "Start New Day",
                    onClick = onStartNewDay,
                )
            }
        }
    }
}

@Composable
private fun HomeSectionChip(
    glyph: String,
    label: String,
    accentColor: String,
) {
    Div(attrs = { attr("style", "display:inline-flex;align-items:center;gap:10px;") }) {
        Div(
            attrs = {
                attr(
                    "style",
                    "display:inline-flex;align-items:center;justify-content:center;width:32px;height:32px;border-radius:12px;background:color-mix(in srgb, $accentColor 12%, white);color:$accentColor;font-size:15px;font-weight:800;"
                )
            }
        ) {
            Text(glyph)
        }
        Div(attrs = { attr("style", "font-size:11px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
            Text(label)
        }
    }
}

@Composable
private fun TonePill(
    text: String,
    background: String,
    foreground: String,
    large: Boolean = false,
) {
    Div(
        attrs = {
            attr(
                "style",
                "display:inline-flex;align-items:center;padding:${if (large) "12px 16px" else "8px 12px"};border-radius:999px;background:$background;color:$foreground;"
            )
        }
    ) {
        Text(text)
    }
}

@Composable
private fun QuietButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        attrs = {
            onClick { onClick() }
            quietButtonStyle()
        }
    ) {
        Text(text)
    }
}

private fun metricGlyph(icon: HomeHeroMetricIcon): String = when (icon) {
    HomeHeroMetricIcon.Focus -> "⌁"
    HomeHeroMetricIcon.Pace -> "↗"
    HomeHeroMetricIcon.Margin -> "△"
    HomeHeroMetricIcon.Gap -> "◷"
    HomeHeroMetricIcon.Clock -> "◴"
    HomeHeroMetricIcon.Trend -> "↗"
    HomeHeroMetricIcon.Target -> "◎"
    HomeHeroMetricIcon.Window -> "◌"
}

private fun AttrsScope<org.w3c.dom.HTMLButtonElement>.quietButtonStyle() {
    attr(
        "style",
        "border:none;background:transparent;color:var(--sa-color-primary);padding:12px 14px;border-radius:999px;font-weight:600;cursor:pointer;"
    )
}

private fun HomeHeroProgressTone.accentColor(): String = when (this) {
    HomeHeroProgressTone.Green -> "var(--sa-color-primary)"
    HomeHeroProgressTone.Yellow -> "var(--sa-color-tertiary)"
    HomeHeroProgressTone.Red -> "var(--sa-color-error)"
    HomeHeroProgressTone.Neutral -> "var(--sa-color-primary-container)"
}

private fun HomeHeroProgressTone.pillBackground(): String = when (this) {
    HomeHeroProgressTone.Green -> "var(--sa-color-primary-container)"
    HomeHeroProgressTone.Yellow -> "var(--sa-color-tertiary-container)"
    HomeHeroProgressTone.Red -> "var(--sa-color-error-container)"
    HomeHeroProgressTone.Neutral -> "rgba(77, 98, 102, 0.14)"
}

private fun HomeHeroProgressTone.pillForeground(): String = when (this) {
    HomeHeroProgressTone.Green -> "var(--sa-color-on-primary-container)"
    HomeHeroProgressTone.Yellow -> "var(--sa-color-on-tertiary-container)"
    HomeHeroProgressTone.Red -> "var(--sa-color-on-error-container)"
    HomeHeroProgressTone.Neutral -> "var(--sa-color-on-secondary-container)"
}

private fun ElapsedTone.pillBackground(): String = when (this) {
    ElapsedTone.Urgent -> "rgba(186, 26, 26, 0.14)"
    ElapsedTone.Warning -> "rgba(132, 70, 0, 0.14)"
    ElapsedTone.Caution -> "rgba(77, 98, 102, 0.14)"
    ElapsedTone.Calm -> "rgba(17, 69, 75, 0.14)"
}

private fun ElapsedTone.pillForeground(): String = when (this) {
    ElapsedTone.Urgent -> "var(--sa-color-error)"
    ElapsedTone.Warning -> "var(--sa-color-on-tertiary-container)"
    ElapsedTone.Caution -> "var(--sa-color-on-secondary-container)"
    ElapsedTone.Calm -> "var(--sa-color-on-primary-container)"
}

private fun ElapsedTone.buttonClass(): String = when (this) {
    ElapsedTone.Urgent -> SmokeWebStyles.elapsedCardUrgent
    ElapsedTone.Warning -> SmokeWebStyles.elapsedCardWarning
    ElapsedTone.Caution -> SmokeWebStyles.elapsedCardCaution
    ElapsedTone.Calm -> SmokeWebStyles.elapsedCardCalm
}
