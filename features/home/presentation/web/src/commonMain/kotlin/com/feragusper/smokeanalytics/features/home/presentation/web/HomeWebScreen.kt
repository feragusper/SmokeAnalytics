package com.feragusper.smokeanalytics.features.home.presentation.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.feragusper.smokeanalytics.features.home.domain.ElapsedTone
import com.feragusper.smokeanalytics.features.home.domain.FinancialSummary
import com.feragusper.smokeanalytics.features.home.domain.GamificationSummary
import com.feragusper.smokeanalytics.features.home.domain.RateSummary
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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun HomeWebScreen(
    deps: HomeWebDependencies,
    onNavigateToHistory: () -> Unit,
    onNavigateToGoals: () -> Unit,
) {
    val store = remember(deps) { HomeWebStore(processHolder = deps.homeProcessHolder) }

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
    val showingInitialSkeleton = error == null && (
        displayLoading ||
            (latestSmokes == null &&
                smokesPerDay == null &&
                smokesPerWeek == null &&
                smokesPerMonth == null &&
                timeSinceLastCigarette == null)
    )

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = greetingTitle ?: "The Pulse",
            eyebrow = "The Pulse",
            badgeText = when {
                displayRefreshLoading -> "Refreshing"
                error != null -> "Needs attention"
                else -> null
            },
            badgeTone = when {
                displayRefreshLoading -> StatusTone.Busy
                error != null -> StatusTone.Error
                else -> StatusTone.Default
            }
        )

        greetingMessage?.let { message ->
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) { Text(message) }
        }

        if (error != null) {
            EmptyStateCard(
                title = if (error == HomeViewState.HomeError.NotLoggedIn) "Session required" else "Could not refresh home",
                message = when (error) {
                    HomeViewState.HomeError.NotLoggedIn -> "The Pulse needs an active session to sync the latest smoke entries and keep the dashboard aligned with your real archive."
                    HomeViewState.HomeError.Generic -> "The home dashboard could not be refreshed. Try again in a moment."
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
            LoadingSkeletonCard(heightPx = 300, lineWidths = listOf("42%", "28%", "68%"))
            LoadingSkeletonCard(heightPx = 70, lineWidths = listOf("36%"))
            Div(attrs = {
                attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;")
            }) {
                repeat(2) { LoadingSkeletonCard(heightPx = 180, lineWidths = listOf("34%", "52%")) }
            }
            LoadingSkeletonCard(heightPx = 160, lineWidths = listOf("24%", "48%", "64%"))
            LoadingSkeletonCard(heightPx = 150, lineWidths = listOf("24%", "48%", "64%"))
        } else {
            PulseHeroCard(
                elapsedTone = elapsedTone,
                timeSinceLastCigarette = timeSinceLastCigarette,
                rateSummary = rateSummary,
            )

            Div(attrs = { attr("style", "display:flex;justify-content:center;") }) {
                PrimaryButton(
                    text = "Track",
                    onClick = { onIntent(HomeIntent.AddSmoke) },
                    enabled = !displayLoading,
                    extraClass = elapsedTone.buttonClass(),
                )
            }

            Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:16px;") }) {
                HomeHighlightCard(
                    eyebrow = "Today's Count",
                    value = smokesPerDay?.toString() ?: "--",
                    supporting = "Cigarettes",
                    onClick = { onIntent(HomeIntent.OnClickHistory) },
                )
                HomeHighlightCard(
                    eyebrow = "Recovery Points",
                    value = gamificationSummary?.points?.toString() ?: "--",
                    supporting = gamificationSummary?.let { "Next ${it.nextMilestoneHours}h" } ?: "Momentum",
                )
            }

            RecoveryStatusCard(
                elapsedTone = elapsedTone,
                timeSinceLastCigarette = timeSinceLastCigarette,
                gamificationSummary = gamificationSummary,
            )

            GoalFocusCard(
                goalProgress = goalProgress,
                hasActiveGoal = hasActiveGoal,
                onOpenGoals = { onIntent(HomeIntent.OnClickGoals) },
            )

            FinancialInsightCard(
                financialSummary = financialSummary,
                rateSummary = rateSummary,
            )

            ArchiveSnapshotCard(
                smokesPerWeek = smokesPerWeek,
                smokesPerMonth = smokesPerMonth,
                rateSummary = rateSummary,
            )

            if (canStartNewDay) {
                EveningResetCard(
                    greetingMessage = greetingMessage,
                    onStartNewDay = { onIntent(HomeIntent.StartNewDay) },
                )
            }
        }
    }
}

@Composable
private fun GoalFocusCard(
    goalProgress: com.feragusper.smokeanalytics.features.goals.domain.GoalProgress?,
    hasActiveGoal: Boolean,
    onOpenGoals: () -> Unit,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
            Div(attrs = { attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                Text("Goal Focus")
            }
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                Text(goalProgress?.title ?: "Add one active goal")
            }
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                Text(goalProgress?.supportingText ?: "Set a daily cap, a reduction target, or a mindful gap and keep it visible from Home.")
            }
            goalProgress?.targetLabel?.let {
                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(it) }
            }
            goalProgress?.progressLabel?.let {
                Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(it) }
            }
            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                PrimaryButton(
                    text = if (hasActiveGoal) "Review in You" else "Set in You",
                    onClick = onOpenGoals,
                )
            }
        }
    }
}

@Composable
private fun PulseHeroCard(
    elapsedTone: ElapsedTone,
    timeSinceLastCigarette: Pair<Long, Long>?,
    rateSummary: RateSummary?,
) {
    val elapsedMinutes = timeSinceLastCigarette?.let { it.first * 60 + it.second }
    val averageGapMinutes = rateSummary?.averageIntervalMinutesToday
    val progress = when {
        elapsedMinutes == null || averageGapMinutes == null || averageGapMinutes <= 0 -> 18
        else -> ((elapsedMinutes.toFloat() / averageGapMinutes).coerceIn(0.08f, 1f) * 100).toInt()
    }

    SurfaceCard {
        Div(attrs = {
            attr(
                "style",
                "display:flex;flex-direction:column;align-items:center;gap:20px;padding:12px 8px 4px;"
            )
        }) {
            Div(attrs = {
                attr(
                    "style",
                    "width:260px;height:260px;border-radius:999px;background:conic-gradient(${elapsedTone.ringColor()} ${progress}%, rgba(20,32,34,0.08) ${progress}% 100%);display:flex;align-items:center;justify-content:center;"
                )
            }) {
                Div(attrs = {
                    attr(
                        "style",
                        "width:216px;height:216px;border-radius:999px;background:var(--sa-color-surface);display:flex;align-items:center;justify-content:center;box-shadow:inset 0 0 0 1px var(--sa-color-outline);"
                    )
                }) {
                    Div(attrs = {
                        attr(
                            "style",
                            "display:flex;flex-direction:column;align-items:center;justify-content:center;gap:6px;text-align:center;"
                        )
                    }) {
                        Div(attrs = {
                            attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:var(--sa-color-secondary);")
                        }) {
                            Text("Mindful Gap")
                        }
                        Div(attrs = {
                            attr("style", "font-size:56px;font-weight:800;line-height:1;color:var(--sa-color-primary);")
                        }) {
                            Text(timeSinceLastCigarette.toPulseValue())
                        }
                        Div(attrs = {
                            attr("style", "font-size:13px;color:var(--sa-color-secondary);")
                        }) {
                            Text("Minutes ago")
                        }
                    }
                }
            }

            Div(attrs = {
                attr(
                    "style",
                    "padding:12px 18px;border-radius:999px;background:${elapsedTone.pillBackground()};color:${elapsedTone.pillForeground()};text-align:center;max-width:640px;"
                )
            }) {
                Text(
                    pulseSummaryText(
                        elapsedMinutes = elapsedMinutes,
                        averageGapMinutes = averageGapMinutes,
                    )
                )
            }
        }
    }
}

@Composable
private fun HomeHighlightCard(
    eyebrow: String,
    value: String,
    supporting: String,
    onClick: (() -> Unit)? = null,
) {
    SurfaceCard {
        Div(attrs = {
            onClick?.let { onClick { it() } }
            attr("style", "display:flex;flex-direction:column;gap:18px;min-height:176px;justify-content:space-between;")
        }) {
            Div(attrs = {
                attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:var(--sa-color-secondary);")
            }) {
                Text(eyebrow)
            }
            Div(attrs = {
                attr("style", "font-size:52px;font-weight:800;line-height:1;color:var(--sa-color-primary);")
            }) {
                Text(value)
            }
            Div(attrs = {
                attr("style", "font-size:14px;color:var(--sa-color-secondary);")
            }) {
                Text(supporting)
            }
        }
    }
}

@Composable
private fun RecoveryStatusCard(
    elapsedTone: ElapsedTone,
    timeSinceLastCigarette: Pair<Long, Long>?,
    gamificationSummary: GamificationSummary?,
) {
    val elapsedMinutes = timeSinceLastCigarette?.let { it.first * 60 + it.second }
    val targetMinutes = (gamificationSummary?.nextMilestoneHours ?: 1).coerceAtLeast(1) * 60
    val progress = if (elapsedMinutes == null) 0 else ((elapsedMinutes.toFloat() / targetMinutes.toFloat()).coerceIn(0f, 1f) * 100).toInt()

    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:16px;") }) {
            Div(attrs = { attr("style", "display:flex;justify-content:space-between;align-items:flex-end;gap:16px;") }) {
                Div {
                    Div(attrs = {
                        attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:var(--sa-color-secondary);margin-bottom:8px;")
                    }) {
                        Text("Recovery Status")
                    }
                    Div(attrs = { attr("style", "font-size:28px;font-weight:800;color:var(--sa-color-primary);") }) {
                        Text(elapsedTone.recoveryTitle())
                    }
                }
                Div(attrs = { attr("style", "font-size:13px;color:var(--sa-color-secondary);") }) {
                    Text("$progress% to next reset")
                }
            }
            ProgressTrack(progress = progress, color = elapsedTone.ringColor())
            Div(attrs = { attr("style", "font-size:14px;color:var(--sa-color-secondary);") }) {
                Text(
                    gamificationSummary?.let { "You are working toward the next ${it.nextMilestoneHours}h milestone." }
                        ?: "Each longer gap compounds into steadier recovery."
                )
            }
        }
    }
}

@Composable
private fun FinancialInsightCard(
    financialSummary: FinancialSummary?,
    rateSummary: RateSummary?,
) {
    val monthTarget = financialSummary?.let { (it.spentWeek * 4).coerceAtLeast(it.spentToday) }
    val progress = if (financialSummary == null || monthTarget == null || monthTarget <= 0.0) 0
    else ((financialSummary.spentToday / monthTarget).coerceIn(0.0, 1.0) * 100).toInt()

    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:16px;") }) {
            Div {
                Div(attrs = {
                    attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:var(--sa-color-secondary);margin-bottom:8px;")
                }) {
                    Text("Financial Insight")
                }
                Div(attrs = { attr("style", "font-size:28px;font-weight:800;color:var(--sa-color-primary);") }) {
                    Text("Budget Mindfulness")
                }
            }

            Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:14px;") }) {
                MetricSummary(
                    label = "Spent today",
                    value = financialSummary?.spentToday?.formatMoney(financialSummary.currencySymbol) ?: "--",
                )
                MetricSummary(
                    label = "Week",
                    value = financialSummary?.spentWeek?.formatMoney(financialSummary.currencySymbol) ?: "--",
                )
                MetricSummary(
                    label = "Month",
                    value = financialSummary?.spentMonth?.formatMoney(financialSummary.currencySymbol) ?: "--",
                    meta = rateSummary?.latestIntervalMinutes?.let { "Latest interval ${it.toGapLabel()}" },
                )
            }

            ProgressTrack(progress = progress, color = "var(--sa-color-primary)")
        }
    }
}

@Composable
private fun ArchiveSnapshotCard(
    smokesPerWeek: Int?,
    smokesPerMonth: Int?,
    rateSummary: RateSummary?,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:16px;") }) {
            Div(attrs = { attr("style", "display:flex;justify-content:space-between;align-items:flex-start;gap:16px;") }) {
                Div {
                    Div(attrs = {
                        attr("style", "font-size:11px;font-weight:700;letter-spacing:0.18em;text-transform:uppercase;color:var(--sa-color-secondary);margin-bottom:8px;")
                    }) {
                        Text("Archive Snapshot")
                    }
                    Div(attrs = { attr("style", "font-size:28px;font-weight:800;color:var(--sa-color-primary);") }) {
                        Text("History holds the full log")
                    }
                }
            }

            Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:14px;") }) {
                MetricSummary(
                    label = "Week",
                    value = smokesPerWeek?.toString() ?: "--",
                    meta = rateSummary?.let { "${it.averageSmokesPerDayWeek.formatOneDecimal()} / day" },
                )
                MetricSummary(
                    label = "Month",
                    value = smokesPerMonth?.toString() ?: "--",
                    meta = rateSummary?.let { "${it.averageSmokesPerDayMonth.formatOneDecimal()} / day" },
                )
            }
        }
    }
}

@Composable
private fun EveningResetCard(
    greetingMessage: String?,
    onStartNewDay: () -> Unit,
) {
    SurfaceCard {
        Div(attrs = {
            attr(
                "style",
                "display:flex;flex-direction:column;align-items:center;text-align:center;gap:14px;background:rgba(121,80,21,0.06);border-radius:22px;padding:28px;"
            )
        }) {
            Div(attrs = { attr("style", "font-size:28px;font-weight:800;color:var(--sa-color-primary);") }) {
                Text("Ready to Reset?")
            }
            Div(attrs = { attr("style", "font-size:14px;color:var(--sa-color-secondary);max-width:480px;") }) {
                Text(greetingMessage ?: "Your day boundary is close. Reset the day when you want to start the next reflection window cleanly.")
            }
            PrimaryButton(text = "Start New Day", onClick = onStartNewDay)
        }
    }
}

@Composable
private fun MetricSummary(
    label: String,
    value: String,
    meta: String? = null,
) {
    Div(attrs = {
        classes(SmokeWebStyles.summaryMetricCard)
        attr("style", "min-height:118px;justify-content:space-between;")
    }) {
        Div(attrs = { classes(SmokeWebStyles.summaryMetricLabel) }) { Text(label) }
        Div(attrs = { classes(SmokeWebStyles.summaryMetricValue) }) { Text(value) }
        meta?.let {
            Div(attrs = { classes(SmokeWebStyles.summaryMetricMeta) }) { Text(it) }
        }
    }
}

@Composable
private fun ProgressTrack(
    progress: Int,
    color: String,
) {
    Div(attrs = {
        attr(
            "style",
            "height:10px;border-radius:999px;background:rgba(20,32,34,0.08);overflow:hidden;"
        )
    }) {
        Div(attrs = {
            attr(
                "style",
                "height:100%;width:${progress.coerceIn(0, 100)}%;border-radius:999px;background:$color;"
            )
        })
    }
}

private fun Pair<Long, Long>?.toPulseValue(): String = this?.let { (hours, minutes) ->
    val totalMinutes = hours * 60 + minutes
    val displayHours = totalMinutes / 60
    val displayMinutes = totalMinutes % 60
    "${displayHours.toString().padStart(2, '0')}:${displayMinutes.toString().padStart(2, '0')}"
} ?: "--:--"

private fun pulseSummaryText(
    elapsedMinutes: Long?,
    averageGapMinutes: Int?,
): String = when {
    elapsedMinutes == null -> "Log a smoke or refresh to rebuild today's pulse."
    averageGapMinutes == null || averageGapMinutes <= 0 -> "Stay with this gap and watch the daily pulse settle."
    elapsedMinutes >= averageGapMinutes -> "You are ${elapsedMinutes - averageGapMinutes} minutes beyond your average gap today."
    else -> "${averageGapMinutes - elapsedMinutes} minutes until you meet today's average gap."
}

private fun Int.toGapLabel(): String = when {
    this >= 60 -> "${this / 60}h ${this % 60}m"
    else -> "${this}m"
}

private fun Double.formatOneDecimal(): String {
    val rounded = (this * 10).toInt() / 10.0
    val whole = rounded.toInt()
    val decimal = ((rounded - whole) * 10).toInt()
    return "$whole.$decimal"
}

private fun ElapsedTone.recoveryTitle(): String = when (this) {
    ElapsedTone.Urgent -> "Level 1 Reset"
    ElapsedTone.Warning -> "Level 2 Recovery"
    ElapsedTone.Caution -> "Level 3 Recovery"
    ElapsedTone.Calm -> "Level 4 Vitality"
}

private fun ElapsedTone.buttonClass(): String = when (this) {
    ElapsedTone.Urgent -> SmokeWebStyles.buttonPrimaryUrgent
    ElapsedTone.Warning -> SmokeWebStyles.buttonPrimaryWarning
    ElapsedTone.Caution -> SmokeWebStyles.buttonPrimaryCaution
    ElapsedTone.Calm -> SmokeWebStyles.buttonPrimaryCalm
}

private fun ElapsedTone.ringColor(): String = when (this) {
    ElapsedTone.Urgent -> "var(--sa-color-danger)"
    ElapsedTone.Warning -> "#B2791F"
    ElapsedTone.Caution -> "#4D7A83"
    ElapsedTone.Calm -> "var(--sa-color-primary)"
}

private fun ElapsedTone.pillBackground(): String = when (this) {
    ElapsedTone.Urgent -> "rgba(186,26,26,0.10)"
    ElapsedTone.Warning -> "rgba(178,121,31,0.12)"
    ElapsedTone.Caution -> "rgba(77,122,131,0.12)"
    ElapsedTone.Calm -> "rgba(46,122,132,0.12)"
}

private fun ElapsedTone.pillForeground(): String = when (this) {
    ElapsedTone.Urgent -> "var(--sa-color-danger)"
    ElapsedTone.Warning -> "#7A4A04"
    ElapsedTone.Caution -> "#2D5D63"
    ElapsedTone.Calm -> "var(--sa-color-primary)"
}

internal fun Instant.toDateInputValue(timeZone: TimeZone): String {
    val ldt = toLocalDateTime(timeZone)
    val mm = ldt.monthNumber.toString().padStart(2, '0')
    val dd = ldt.dayOfMonth.toString().padStart(2, '0')
    return "${ldt.year}-$mm-$dd"
}

internal fun Instant.toTimeInputValue(timeZone: TimeZone): String {
    val ldt = toLocalDateTime(timeZone)
    val hh = ldt.hour.toString().padStart(2, '0')
    val mm = ldt.minute.toString().padStart(2, '0')
    return "$hh:$mm"
}

internal fun dateTimeInputsToInstant(
    dateValue: String,
    timeValue: String,
    timeZone: TimeZone,
): Instant {
    val date = LocalDate.parse(dateValue)
    val time = LocalTime.parse(timeValue)
    val ldt = LocalDateTime(
        year = date.year,
        monthNumber = date.monthNumber,
        dayOfMonth = date.dayOfMonth,
        hour = time.hour,
        minute = time.minute,
        second = 0,
        nanosecond = 0,
    )
    return ldt.toInstant(timeZone)
}
