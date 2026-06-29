package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingStats
import kotlinx.coroutines.delay
import kotlin.time.Clock
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
import org.jetbrains.compose.web.dom.Span
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
    val hasLoadedContent = smokesPerDay != null || timeSinceLastCigarette != null || goalProgress != null
    val showingInitialSkeleton = error == null && (
        displayLoading ||
            !hasLoadedContent
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

        if (error != null && !hasLoadedContent) {
            return@Div
        } else if (showingInitialSkeleton) {
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

            cravingCelebration?.let { celebration ->
                CravingCelebrationCard(
                    celebration = celebration,
                    onDismiss = { onIntent(HomeIntent.DismissCravingCelebration) },
                )
            }

            if (showCravingHint) {
                CravingHintCard(onDismiss = { onIntent(HomeIntent.DismissCravingHint) })
            }

            activeCraving.let { craving ->
                if (craving != null) {
                    CravingCountdownCard(
                        craving = craving,
                        onResolve = { smoked ->
                            onIntent(HomeIntent.ResolveCraving(craving = craving, smoked = smoked))
                        },
                    )
                } else {
                    CravingPromptCard(onTrack = { onIntent(HomeIntent.TrackCraving) })
                }
            }

            if (pendingRelationshipSmokes.isNotEmpty()) {
                RelationshipReminderCardWeb(
                    pendingCount = pendingRelationshipSmokes.size,
                    onAdd = {
                        pendingRelationshipSmokes.firstOrNull()?.let {
                            onIntent(HomeIntent.OpenRelationshipPrompt(it.id))
                        }
                    },
                )
            }

            HomeInsightGrid(
                lastSmokeTimeLabel = lastSmoke?.date?.toHomeClockLabel(),
                timeSinceLastCigarette = timeSinceLastCigarette,
                gapFocus = gapFocus,
                consistencyLabel = narrative.consistencyLabel,
                statusLabel = narrative.statusLabel,
                elapsedTone = elapsedTone,
            )

            cravingStats?.takeIf { it.total > 0 }?.let { stats ->
                CravingStatsCard(stats = stats)
            }

            monthTrend?.let { trendValue ->
                HomeTrendCard(trendValue = trendValue, delta = monthTrendDelta)
            }

            if (canStartNewDay) {
                EveningResetCard(
                    onStartNewDay = { onIntent(HomeIntent.StartNewDay) },
                )
            }
        }
    }

    val promptSmokeId = relationshipPromptSmokeId
    if (promptSmokeId != null) {
        RelationshipPromptDialogWeb(
            onSave = { triggers, note ->
                onIntent(HomeIntent.SaveSmokeRelationship(promptSmokeId, triggers, note))
            },
            onSkip = { onIntent(HomeIntent.SkipSmokeRelationship(promptSmokeId)) },
            onDismiss = { onIntent(HomeIntent.DismissRelationshipPrompt) },
        )
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
    elapsedTone: ElapsedTone,
) {
    Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:14px;") }) {
        LastCigaretteCard(
            lastSmokeTimeLabel = lastSmokeTimeLabel,
            timeSinceLastCigarette = timeSinceLastCigarette,
            gapFocus = gapFocus,
            elapsedTone = elapsedTone,
        )
        ConsistencyCard(
            consistencyLabel = consistencyLabel,
            statusLabel = statusLabel,
        )
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
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            HomeSectionChip("◷", "Last cigarette", elapsedTone.pillForeground())
            Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px;") }) {
                LastCigaretteValueCard(
                    label = "At",
                    value = lastSmokeTimeLabel?.let { "$it hs" } ?: "--:--",
                )
                LastCigaretteValueCard(
                    label = "Time since",
                    value = timeSinceLastCigarette.toElapsedGapLabel(),
                )
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
            )
        }
    }
}

@Composable
private fun LastCigaretteValueCard(
    label: String,
    value: String,
) {
    Div(
        attrs = {
            attr(
                "style",
                "display:flex;flex-direction:column;justify-content:space-between;gap:12px;min-height:104px;padding:16px;border-radius:22px;background:var(--sa-color-surface-strong);"
            )
        }
    ) {
        Div(attrs = { classes(SmokeWebStyles.helperText) }) {
            Text(label)
        }
        Div(attrs = { attr("style", "font-size:28px;font-weight:800;line-height:1.05;color:var(--sa-color-primary);") }) {
            Text(value)
        }
    }
}

@Composable
private fun ConsistencyCard(
    consistencyLabel: String,
    statusLabel: String,
) {
    val streakDays = consistencyLabel.completedStreakDays()

    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            HomeSectionChip("↗", "Consistency", "var(--sa-color-primary)")
            Div(attrs = { attr("style", "font-size:28px;font-weight:800;line-height:1.1;color:var(--sa-color-on-surface);") }) {
                Text(consistencyLabel)
            }
            ConsistencyMilestones(streakDays = streakDays)
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text(statusLabel)
            }
        }
    }
}

@Composable
private fun ConsistencyMilestones(
    streakDays: Int?,
) {
    val milestones = listOf(
        ConsistencyMilestone(days = 7, glyph = "✓"),
        ConsistencyMilestone(days = 14, glyph = "↟"),
        ConsistencyMilestone(days = 30, glyph = "★"),
        ConsistencyMilestone(days = 60, glyph = "◇"),
    )
    Div(attrs = { attr("style", "display:flex;flex-wrap:wrap;gap:8px;") }) {
        milestones.forEach { milestone ->
            val completed = streakDays != null && streakDays >= milestone.days
            Div(
                attrs = {
                    attr(
                        "style",
                        "display:inline-flex;align-items:center;gap:6px;padding:8px 10px;border-radius:999px;" +
                            "background:${if (completed) "var(--sa-color-primary-container)" else "var(--sa-color-surface-strong)"};" +
                            "color:${if (completed) "var(--sa-color-on-primary-container)" else "var(--sa-color-secondary)"};" +
                            "box-shadow:inset 0 0 0 1px rgba(17, 69, 75, 0.08);font-size:12px;font-weight:800;"
                    )
                }
            ) {
                Span { Text(milestone.glyph) }
                Span { Text("${milestone.days}d") }
            }
        }
    }
}

private data class ConsistencyMilestone(
    val days: Int,
    val glyph: String,
)

@Composable
private fun CravingPromptCard(onTrack: () -> Unit) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            HomeSectionChip("♥", "Craving", "var(--sa-color-primary)")
            Div(attrs = { attr("style", "font-size:18px;font-weight:800;") }) {
                Text("Feeling the urge?")
            }
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                Text("Track the craving before lighting up. If it isn't time yet, we'll help you wait it out and reward the win.")
            }
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                PrimaryButton(text = "I feel like smoking", onClick = onTrack)
            }
        }
    }
}

@Composable
private fun CravingHintCard(onDismiss: () -> Unit) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;align-items:center;justify-content:space-between;gap:14px;") }) {
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                Text("It's already a good time — go ahead when you want.")
            }
            CravingTextButton(text = "Dismiss", onClick = onDismiss)
        }
    }
}

@Composable
private fun CravingCountdownCard(
    craving: Craving,
    onResolve: (smoked: Boolean) -> Unit,
) {
    val target = craving.targetAt
    var remaining by remember(craving.id) {
        mutableStateOf(target?.let { (it - Clock.System.now()).inWholeSeconds } ?: 0L)
    }
    LaunchedEffect(craving.id) {
        if (target != null) {
            while (true) {
                remaining = (target - Clock.System.now()).inWholeSeconds
                if (remaining <= 0L) break
                delay(1_000)
            }
        }
    }
    val done = remaining <= 0L

    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;align-items:center;text-align:center;") }) {
            Div(attrs = { attr("style", "font-size:20px;font-weight:800;") }) {
                Text(if (done) "You made it! 🎉" else "Hold on 💪")
            }
            if (done) {
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text("The wait is over. Log the cigarette if you still want it, or let the urge pass for the full reward.")
                }
            } else {
                Div(attrs = { attr("style", "font-size:48px;font-weight:800;line-height:1;color:var(--sa-color-primary);") }) {
                    Text(remaining.toCountdownLabel())
                }
                Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                    Text("Until your next cigarette fits the goal. You've got this.")
                }
            }
            // onResolve(true)  -> smoked (gave in while waiting / postponed once done)
            // onResolve(false) -> the urge passed without smoking (resisted)
            Div(attrs = { attr("style", "display:flex;gap:12px;width:100%;justify-content:center;") }) {
                if (done) {
                    // The wait paid off: log the now-allowed cigarette, or let it go.
                    CravingTextButton(text = "I'm good", onClick = { onResolve(false) })
                    PrimaryButton(text = "Log the cigarette", onClick = { onResolve(true) })
                } else {
                    // While waiting the only manual action is the give-in escape hatch.
                    // Resisting is automatic when the countdown ends.
                    CravingTextButton(text = "I smoked anyway", onClick = { onResolve(true) })
                }
            }
        }
    }
}

@Composable
private fun CravingCelebrationCard(
    celebration: HomeViewState.CravingCelebration,
    onDismiss: () -> Unit,
) {
    val resisted = celebration.outcome == CravingOutcome.RESISTED
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;align-items:center;justify-content:space-between;gap:14px;background:linear-gradient(135deg,var(--sa-color-primary) 0%, #2D5D63 100%);border-radius:24px;padding:20px;color:var(--sa-color-onPrimary);") }) {
            Div {
                Div(attrs = { attr("style", "font-size:18px;font-weight:800;") }) {
                    Text(if (resisted) "🏆 Urge beaten!" else "🏆 Nice and slow")
                }
                Div(attrs = { attr("style", "font-size:14px;opacity:0.85;margin-top:6px;") }) {
                    Text(
                        if (resisted) {
                            "You let the craving pass without smoking. +${celebration.points} points earned."
                        } else {
                            "You waited it out before smoking. +${celebration.points} points earned."
                        }
                    )
                }
            }
            CravingTextButton(text = "Nice", onClick = onDismiss)
        }
    }
}

@Composable
private fun CravingStatsCard(stats: CravingStats) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            HomeSectionChip("♥", "Cravings", "var(--sa-color-primary)")
            Div(attrs = { attr("style", "display:flex;gap:12px;flex-wrap:wrap;") }) {
                CravingStatCell(value = "${stats.resisted}", label = "Resisted")
                CravingStatCell(value = "${stats.postponed}", label = "Postponed")
                CravingStatCell(value = stats.minutesWaited.toWaitedLabel(), label = "Waited")
                CravingStatCell(value = "${stats.points}", label = "Points")
            }
        }
    }
}

@Composable
private fun CravingStatCell(value: String, label: String) {
    Div(attrs = {
        attr(
            "style",
            "flex:1;min-width:90px;background:var(--sa-color-surfaceVariant,#F1F4F4);border-radius:18px;padding:14px;display:flex;flex-direction:column;gap:4px;"
        )
    }) {
        Div(attrs = { attr("style", "font-size:24px;font-weight:800;") }) { Text(value) }
        Div(attrs = { attr("style", "font-size:12px;opacity:0.7;") }) { Text(label) }
    }
}

@Composable
private fun CravingTextButton(text: String, onClick: () -> Unit) {
    Button(attrs = {
        onClick { onClick() }
        attr(
            "style",
            "background:transparent;border:1px solid rgba(17,69,75,0.28);color:inherit;border-radius:14px;padding:10px 18px;font-weight:700;cursor:pointer;"
        )
    }) {
        Text(text)
    }
}

private fun Long.toCountdownLabel(): String {
    val total = if (this < 0) 0 else this
    val minutes = total / 60
    val seconds = total % 60
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        "$hours:${mins.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${mins.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}

private fun Long.toWaitedLabel(): String {
    val minutes = if (this < 0) 0 else this
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours <= 0 -> "${mins}m"
        mins == 0L -> "${hours}h"
        else -> "${hours}h ${mins}m"
    }
}

@Composable
private fun HomeTrendCard(
    trendValue: Int,
    delta: Int?,
) {
    // trendValue is the reduction vs last month: positive = smoking less (good),
    // negative = smoking more (bad). Colour and copy follow that, not a fixed green.
    val improving = trendValue > 0
    val worsening = trendValue < 0
    val gradient = when {
        improving -> "linear-gradient(135deg,var(--sa-color-primary) 0%, #2D5D63 100%)"
        worsening -> "linear-gradient(135deg,#C0413B 0%, #8E2B27 100%)"
        else -> "linear-gradient(135deg,#5A6568 0%, #3E484B 100%)"
    }
    val headline = when {
        improving -> "Smoking less than last month"
        worsening -> "Smoking more than last month"
        else -> "Same pace as last month"
    }
    val deltaLabel = delta?.let {
        when {
            it < 0 -> "${-it} fewer ${pluralCigarettes(-it)} so far"
            it > 0 -> "$it more ${pluralCigarettes(it)} so far"
            else -> "Same count so far"
        }
    }
    val glyph = if (improving) "↘" else if (worsening) "↗" else "→"

    SurfaceCard {
        Div(attrs = {
            attr(
                "style",
                "display:flex;justify-content:space-between;align-items:center;gap:18px;background:$gradient;border-radius:24px;padding:24px;color:#FFFFFF;"
            )
        }) {
            Div {
                Div(attrs = { attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;opacity:0.76;") }) {
                    Text("Vs last month")
                }
                Div(attrs = { attr("style", "font-size:26px;font-weight:800;line-height:1.1;margin-top:10px;max-width:320px;") }) {
                    Text(headline)
                }
                deltaLabel?.let {
                    Div(attrs = { attr("style", "font-size:14px;opacity:0.82;margin-top:8px;") }) {
                        Text(it)
                    }
                }
            }
            Div(attrs = {
                attr(
                    "style",
                    "width:84px;height:84px;border-radius:999px;border:8px solid rgba(255,255,255,0.22);display:flex;flex-direction:column;align-items:center;justify-content:center;flex-shrink:0;"
                )
            }) {
                Div(attrs = { attr("style", "font-size:26px;font-weight:800;line-height:1;") }) { Text(glyph) }
                Div(attrs = { attr("style", "font-size:14px;font-weight:700;") }) {
                    Text("${if (trendValue > 0) "+" else ""}$trendValue%")
                }
            }
        }
    }
}

private fun pluralCigarettes(count: Int): String = if (count == 1) "cigarette" else "cigarettes"

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

private fun String.completedStreakDays(): Int? =
    Regex("""(\d+)\s+days?\s+completed\s+in\s+a\s+row""")
        .find(this)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()

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
