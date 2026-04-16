package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.home.domain.ElapsedTone
import com.feragusper.smokeanalytics.features.home.domain.FinancialSummary
import com.feragusper.smokeanalytics.features.home.domain.GapFocusSummary
import com.feragusper.smokeanalytics.features.home.domain.HomeHeroProgressTone
import com.feragusper.smokeanalytics.features.home.domain.gapFocusSummary
import com.feragusper.smokeanalytics.features.home.domain.homeHeroProgress
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
import com.feragusper.smokeanalytics.libraries.preferences.domain.formatMoney
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
            )

            LastCigaretteCard(
                lastSmokeTimeLabel = lastSmoke?.date?.toHomeClockLabel(),
                timeSinceLastCigarette = timeSinceLastCigarette,
                gapFocus = gapFocus,
                elapsedTone = elapsedTone,
            )

            ConsistencyCard(
                consistencyLabel = narrative.consistencyLabel,
                statusLabel = narrative.statusLabel,
            )

            NextActionCard(
                supporting = narrative.nextActionLabel,
                secondaryLabel = if (hasActiveGoal) "Review in You" else "Set in You",
                elapsedTone = elapsedTone,
                onAddSmoke = { onIntent(HomeIntent.AddSmoke) },
                onOpenGoals = { onIntent(HomeIntent.OnClickGoals) },
            )

            if (smokesPerDay != null || financialSummary != null) {
                SupportMetricsRow(
                    smokesPerDay = smokesPerDay,
                    financialSummary = financialSummary,
                )
            }

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
) {
    SurfaceCard {
        Div(
            attrs = {
                attr(
                    "style",
                    "display:flex;gap:20px;align-items:center;justify-content:space-between;flex-wrap:wrap;"
                )
            }
        ) {
            GoalHeroProgress(heroProgress = heroProgress)
            Div(
                attrs = {
                    attr("style", "display:flex;flex-direction:column;gap:12px;flex:1;min-width:240px;")
                }
            ) {
                SectionEyebrow("Goal")
                Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                    Text(narrative.heroTitle)
                }
                TonePill(
                    text = narrative.statusLabel,
                    background = heroProgress.tone.pillBackground(),
                    foreground = heroProgress.tone.pillForeground(),
                )
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text(narrative.heroSupporting)
                }
            }
        }
    }
}

@Composable
private fun GoalHeroProgress(
    heroProgress: com.feragusper.smokeanalytics.features.home.domain.HomeHeroProgress,
) {
    val bounded = heroProgress.fraction.coerceIn(0.08f, 1f)
    val percent = (bounded * 100).toInt()
    Div(
        attrs = {
            attr(
                "style",
                "width:124px;height:124px;border-radius:999px;background:conic-gradient(${heroProgress.tone.ringColor()} ${percent}%, rgba(20,32,34,0.08) ${percent}% 100%);display:flex;align-items:center;justify-content:center;flex:0 0 auto;"
            )
        }
    ) {
        Div(
            attrs = {
                attr(
                    "style",
                    "width:94px;height:94px;border-radius:999px;background:var(--sa-color-surface);display:flex;align-items:center;justify-content:center;"
                )
            }
        ) {
            Div(attrs = { attr("style", "font-size:18px;font-weight:700;color:var(--sa-color-primary);") }) {
                Text("${percent}%")
            }
        }
    }
}

@Composable
private fun LastCigaretteCard(
    lastSmokeTimeLabel: String?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    gapFocus: GapFocusSummary,
    elapsedTone: ElapsedTone,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:16px;") }) {
            SectionEyebrow("Last cigarette")
            Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:14px;") }) {
                KeyValueCard("At", lastSmokeTimeLabel?.let { "$it hs" } ?: "--:--")
                KeyValueCard("Time since", timeSinceLastCigarette.toElapsedGapLabel(), emphasize = true)
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
private fun ConsistencyCard(
    consistencyLabel: String,
    statusLabel: String,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:10px;") }) {
            SectionEyebrow("Consistency")
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                Text(consistencyLabel)
            }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text(statusLabel)
            }
        }
    }
}

@Composable
private fun NextActionCard(
    supporting: String,
    secondaryLabel: String,
    elapsedTone: ElapsedTone,
    onAddSmoke: () -> Unit,
    onOpenGoals: () -> Unit,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            SectionEyebrow("Next action")
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                Text(supporting)
            }
            Div(attrs = { attr("style", "display:flex;gap:12px;flex-wrap:wrap;") }) {
                PrimaryButton(
                    text = "Track",
                    onClick = onAddSmoke,
                    extraClass = elapsedTone.buttonClass(),
                )
                QuietButton(
                    text = secondaryLabel,
                    onClick = onOpenGoals,
                )
            }
        }
    }
}

@Composable
private fun SupportMetricsRow(
    smokesPerDay: Int?,
    financialSummary: FinancialSummary?,
) {
    Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:14px;") }) {
        KeyValueCard("Today", smokesPerDay?.toString() ?: "--", supporting = "Cigarettes")
        KeyValueCard(
            "Spent",
            financialSummary?.spentToday?.formatMoney(financialSummary.currencySymbol) ?: "--",
            supporting = "Today",
        )
    }
}

@Composable
private fun EveningResetCard(
    onStartNewDay: () -> Unit,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            SectionEyebrow("Starting early?")
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
private fun SectionEyebrow(text: String) {
    Div(attrs = { attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
        Text(text)
    }
}

@Composable
private fun KeyValueCard(
    label: String,
    value: String,
    supporting: String? = null,
    emphasize: Boolean = false,
) {
    Div(
        attrs = {
            attr(
                "style",
                "display:flex;flex-direction:column;gap:8px;padding:16px 18px;border-radius:24px;background:var(--sa-color-surface-container-low);"
            )
        }
    ) {
        Div(attrs = { attr("style", "font-size:11px;font-weight:700;letter-spacing:0.12em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
            Text(label)
        }
        Div(
            attrs = {
                attr(
                    "style",
                    "font-size:${if (emphasize) 34 else 28}px;font-weight:800;line-height:1.1;color:var(--sa-color-primary);"
                )
            }
        ) {
            Text(value)
        }
        supporting?.let {
            Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(it) }
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

private fun AttrsScope<org.w3c.dom.HTMLButtonElement>.quietButtonStyle() {
    attr(
        "style",
        "border:none;background:transparent;color:var(--sa-color-primary);padding:12px 14px;border-radius:999px;font-weight:600;cursor:pointer;"
    )
}

private fun HomeHeroProgressTone.ringColor(): String = when (this) {
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
