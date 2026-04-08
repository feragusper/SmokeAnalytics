package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotUseCase
import com.feragusper.smokeanalytics.features.chatbot.domain.CoachReplySource
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.LoadingSkeletonCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.StatusTone
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun CoachWebScreen(
    chatbotUseCase: ChatbotUseCase,
) {
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf(emptyList<CoachMessage>()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun loadInitialInsight() {
        loading = true
        error = null
        runCatching { chatbotUseCase.sendInitialMessageWithContext() }
            .onSuccess { reply ->
                messages = listOf(
                    CoachMessage(
                        title = "Primary insight",
                        body = reply.text,
                        source = reply.source,
                    ),
                )
            }
            .onFailure {
                error = "The coach could not prepare your insight right now. Try again in a moment."
            }
        loading = false
    }

    suspend fun sendPrompt(prompt: CoachPrompt) {
        loading = true
        error = null
        runCatching { chatbotUseCase.sendMessage(prompt.message) }
            .onSuccess { reply ->
                messages = messages + CoachMessage(
                    title = prompt.label,
                    body = reply.text,
                    source = reply.source,
                )
            }
            .onFailure {
                error = "The coach could not answer that prompt. Try again in a moment."
            }
        loading = false
    }

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            loadInitialInsight()
        }
    }

    val primaryInsight = messages.firstOrNull()
    val secondaryInsights = messages.drop(1)
    val hasFallback = messages.any { it.source == CoachReplySource.Fallback }
    val guideMode = remember(primaryInsight, hasFallback, loading, error) {
        when {
            loading && primaryInsight == null -> GuideUiMode.Loading
            primaryInsight != null && hasFallback -> GuideUiMode.Fallback
            primaryInsight != null -> GuideUiMode.Live
            error != null -> GuideUiMode.Unavailable
            else -> GuideUiMode.Quiet
        }
    }

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "The Guide",
            eyebrow = "Guide",
            subtitle = "A calmer coaching surface for cravings, progress checks, and pattern shifts built around recent smoking behavior.",
            badgeText = guideMode.badgeText,
            badgeTone = guideMode.badgeTone,
            actions = {
                GhostButton(
                    text = if (guideMode == GuideUiMode.Unavailable) "Retry" else "Refresh",
                    onClick = { scope.launch { loadInitialInsight() } },
                    enabled = !loading,
                )
            },
        )

        when {
            guideMode == GuideUiMode.Loading -> {
                GuideTopSummary(mode = guideMode)
                LoadingSkeletonCard(heightPx = 220, lineWidths = listOf("28%", "76%", "62%"))
                LoadingSkeletonCard(heightPx = 140, lineWidths = listOf("22%", "58%"))
            }

            guideMode == GuideUiMode.Quiet -> {
                GuideTopSummary(mode = guideMode)
                EmptyStateCard(
                    title = "No guide insight yet",
                    message = "Refresh the guide to build a new insight from your recent smoking pattern and current session context.",
                    actionLabel = "Refresh",
                    onAction = { scope.launch { loadInitialInsight() } },
                )
            }

            guideMode == GuideUiMode.Unavailable -> {
                GuideTopSummary(mode = guideMode)
                EmptyStateCard(
                    title = "Guide temporarily unavailable",
                    message = error ?: "The coach could not prepare your insight right now. Try again in a moment.",
                    actionLabel = "Retry",
                    onAction = { scope.launch { loadInitialInsight() } },
                )
            }

            else -> {
                GuideTopSummary(mode = guideMode)
                PrimaryInsightCard(
                    message = requireNotNull(primaryInsight),
                    hasFallback = guideMode == GuideUiMode.Fallback,
                    onPrimaryAction = {
                        scope.launch { sendPrompt(primaryCoachActions.first()) }
                    },
                    onSecondaryAction = {
                        scope.launch { sendPrompt(primaryCoachActions.last()) }
                    },
                )

                Div(attrs = {
                    attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;")
                }) {
                    buildSupportCards(
                        messages = messages,
                        hasFallback = hasFallback,
                    ).forEach { card ->
                        InsightSupportCard(
                            icon = card.icon,
                            title = card.title,
                            body = card.body,
                        )
                    }
                }

                Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:14px;") }) {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                        Text("Ask By Intent")
                    }
                    coachActionGroups.forEach { group ->
                        ActionGroupCard(
                            group = group,
                            loading = loading,
                            onPromptClick = { prompt -> scope.launch { sendPrompt(prompt) } },
                        )
                    }
                }

                Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                        Text("Quiet Support")
                    }
                    coachTips.forEach { tip ->
                        TipCard(
                            icon = tip.icon,
                            title = tip.title,
                            body = tip.body,
                        )
                    }
                }

                if (secondaryInsights.isNotEmpty()) {
                    Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
                        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                            Text("Recent Follow-ups")
                        }
                        secondaryInsights.forEach { message ->
                            FollowUpCard(message = message)
                        }
                    }
                }

                WeeklySummaryCard(
                    hasFallback = guideMode == GuideUiMode.Fallback,
                    totalInsights = messages.size,
                    totalPrompts = secondaryInsights.size,
                )
            }
        }
    }
}

private enum class GuideUiMode(
    val badgeText: String,
    val badgeTone: StatusTone,
) {
    Loading("Refreshing", StatusTone.Busy),
    Live("Live", StatusTone.Default),
    Fallback("Fallback", StatusTone.Error),
    Quiet("Quiet", StatusTone.Default),
    Unavailable("Unavailable", StatusTone.Error),
}

@Composable
private fun GuideTopSummary(
    mode: GuideUiMode,
) {
    Div(attrs = {
        attr("style", "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;")
    }) {
        GuideContextCard(
            title = "What it uses",
            accent = "Context",
            body = "Recent smoking rhythm, recovery gaps, and the prompts you ask in this session."
        )
        GuideContextCard(
            title = "Best asks",
            accent = "Intent",
            body = "Craving plans, stress resets, delay tactics, and weekly pattern checks work best here."
        )
        GuideContextCard(
            title = when (mode) {
                GuideUiMode.Loading -> "Refreshing"
                GuideUiMode.Live -> "Live guidance"
                GuideUiMode.Fallback -> "Fallback mode"
                GuideUiMode.Quiet -> "Quiet state"
                GuideUiMode.Unavailable -> "Unavailable"
            },
            accent = when (mode) {
                GuideUiMode.Loading -> "Busy"
                GuideUiMode.Live -> "Live"
                GuideUiMode.Fallback -> "Fallback"
                GuideUiMode.Quiet -> "Quiet"
                GuideUiMode.Unavailable -> "Retry"
            },
            body = when (mode) {
                GuideUiMode.Loading -> "Refreshing the next guide insight now."
                GuideUiMode.Live -> "Live coaching is active, so the guide can respond with more tailored follow-ups."
                GuideUiMode.Fallback -> "Fallback guidance is active, so answers stay practical and bounded even without the live model."
                GuideUiMode.Quiet -> "The guide is waiting for enough recent context to produce a focused insight."
                GuideUiMode.Unavailable -> "The coach could not prepare an insight right now. Retry in a moment."
            }
        )
    }
}

@Composable
private fun PrimaryInsightCard(
    message: CoachMessage,
    hasFallback: Boolean,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "position:relative;overflow:hidden;padding:8px 4px;") }) {
            Div(attrs = {
                attr(
                    "style",
                    "position:absolute;top:-32px;right:-32px;width:120px;height:120px;border-radius:999px;background:rgba(17,69,75,0.06);"
                )
            })
            Div(attrs = { attr("style", "display:flex;align-items:center;gap:8px;margin-bottom:18px;position:relative;z-index:1;") }) {
                Div(attrs = { attr("style", "color:var(--sa-color-primary);font-weight:700;font-size:20px;") }) { Text("◎") }
                Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-secondary);") }) {
                    Text(if (hasFallback) "Fallback Insight" else "Primary Insight")
                }
            }
            Div(attrs = { attr("style", "font-size:34px;font-weight:800;line-height:1.12;color:var(--sa-color-primary);max-width:640px;") }) {
                Text(message.body)
            }
            Div(attrs = { attr("style", "margin-top:14px;font-size:16px;line-height:1.6;color:var(--sa-color-secondary);max-width:720px;") }) {
                Text(
                    if (hasFallback) {
                        "Live coaching is unavailable right now. The guide is still using your recent smoking context, but the response is coming from the built-in fallback path."
                    } else {
                        "Use this insight to decide what to ask next: decode the pattern, get a realistic adjustment, or ask for a short recovery plan."
                    }
                )
            }
            Div(attrs = { attr("style", "display:flex;flex-wrap:wrap;gap:12px;margin-top:24px;") }) {
                Button(attrs = {
                    attr(
                        "style",
                        "background:var(--sa-color-primary);color:var(--sa-color-onPrimary);padding:14px 18px;border:none;border-radius:18px;font-weight:700;cursor:pointer;"
                    )
                    onClick { onPrimaryAction() }
                }) {
                    Text("How can I adjust this?")
                }
                Button(attrs = {
                    attr(
                        "style",
                        "background:var(--sa-color-secondaryContainer);color:var(--sa-color-onSecondaryContainer);padding:14px 18px;border:none;border-radius:18px;font-weight:700;cursor:pointer;"
                    )
                    onClick { onSecondaryAction() }
                }) {
                    Text("Tell me more")
                }
            }
        }
    }
}

@Composable
private fun GuideContextCard(
    title: String,
    accent: String,
    body: String,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:8px;min-height:160px;") }) {
            Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:var(--sa-color-primary);") }) {
                Text(accent)
            }
            Div(attrs = { attr("style", "font-size:20px;font-weight:700;color:var(--sa-color-onSurface);") }) {
                Text(title)
            }
            Div(attrs = { attr("style", "font-size:14px;line-height:1.6;color:var(--sa-color-secondary);") }) {
                Text(body)
            }
        }
    }
}

@Composable
private fun InsightSupportCard(
    icon: String,
    title: String,
    body: String,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;min-height:210px;") }) {
            Div(attrs = { attr("style", "font-size:20px;color:var(--sa-color-primary);font-weight:700;") }) {
                Text(icon)
            }
            Div(attrs = { attr("style", "font-size:20px;font-weight:700;color:var(--sa-color-onSurface);") }) {
                Text(title)
            }
            Div(attrs = { attr("style", "font-size:14px;line-height:1.6;color:var(--sa-color-secondary);") }) {
                Text(body)
            }
        }
    }
}

@Composable
private fun ActionGroupCard(
    group: CoachActionGroup,
    loading: Boolean,
    onPromptClick: (CoachPrompt) -> Unit,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
            Div(attrs = { attr("style", "font-size:20px;font-weight:700;color:var(--sa-color-onSurface);") }) {
                Text(group.title)
            }
            Div(attrs = { attr("style", "font-size:14px;line-height:1.6;color:var(--sa-color-secondary);") }) {
                Text(group.subtitle)
            }
            Div(attrs = { attr("style", "display:flex;flex-wrap:wrap;gap:12px;") }) {
                group.actions.forEach { prompt ->
                    PrimaryButton(
                        text = prompt.label,
                        onClick = { onPromptClick(prompt) },
                        enabled = !loading,
                    )
                }
            }
        }
    }
}

@Composable
private fun TipCard(
    icon: String,
    title: String,
    body: String,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;gap:16px;align-items:flex-start;") }) {
            Div(attrs = {
                attr(
                    "style",
                    "width:44px;height:44px;border-radius:16px;background:var(--sa-color-surface);display:flex;align-items:center;justify-content:center;font-weight:700;color:var(--sa-color-primary);flex:none;"
                )
            }) {
                Text(icon)
            }
            Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:6px;") }) {
                Div(attrs = { attr("style", "font-size:16px;font-weight:700;color:var(--sa-color-onSurface);") }) {
                    Text(title)
                }
                Div(attrs = { attr("style", "font-size:14px;line-height:1.6;color:var(--sa-color-secondary);") }) {
                    Text(body)
                }
            }
        }
    }
}

@Composable
private fun FollowUpCard(
    message: CoachMessage,
) {
    SurfaceCard {
        Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:8px;") }) {
            Div(attrs = {
                attr(
                    "style",
                    "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;color:${if (message.source == CoachReplySource.Live) "var(--sa-color-primary)" else "#ba1a1a"};"
                )
            }) {
                Text(message.title)
            }
            Div(attrs = { attr("style", "font-size:16px;line-height:1.65;color:var(--sa-color-onSurface);") }) {
                Text(message.body)
            }
        }
    }
}

@Composable
private fun WeeklySummaryCard(
    hasFallback: Boolean,
    totalInsights: Int,
    totalPrompts: Int,
) {
    SurfaceCard {
        Div(attrs = {
            attr(
                "style",
                "display:flex;flex-direction:column;gap:20px;background:linear-gradient(135deg,var(--sa-color-primary) 0%, #2D5D63 100%);border-radius:24px;padding:24px;color:var(--sa-color-onPrimary);"
            )
        }) {
            Div(attrs = { attr("style", "display:flex;justify-content:space-between;align-items:flex-start;gap:16px;") }) {
                Div {
                    Div(attrs = { attr("style", "font-size:12px;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;opacity:0.78;") }) {
                        Text("Guide Snapshot")
                    }
                    Div(attrs = { attr("style", "font-size:32px;font-weight:800;line-height:1.1;margin-top:6px;") }) {
                        Text(if (hasFallback) "Steady fallback support" else "Live coaching session")
                    }
                }
                Div(attrs = { attr("style", "font-size:28px;opacity:0.42;") }) {
                    Text("↘")
                }
            }
            Div(attrs = { attr("style", "display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;") }) {
                SummaryMetric(title = "Guide mode", value = if (hasFallback) "Fallback" else "Live")
                SummaryMetric(title = "Insights", value = totalInsights.toString())
                SummaryMetric(title = "Prompts", value = totalPrompts.toString())
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    title: String,
    value: String,
) {
    Div(attrs = {
        attr(
            "style",
            "background:rgba(255,255,255,0.12);border-radius:18px;padding:16px;display:flex;flex-direction:column;gap:6px;"
        )
    }) {
        Div(attrs = { attr("style", "font-size:11px;opacity:0.76;letter-spacing:0.08em;text-transform:uppercase;") }) {
            Text(title)
        }
        Div(attrs = { attr("style", "font-size:28px;font-weight:800;line-height:1;") }) {
            Text(value)
        }
    }
}

private data class CoachMessage(
    val title: String,
    val body: String,
    val source: CoachReplySource,
)

private data class CoachPrompt(
    val label: String,
    val message: String,
)

private data class CoachActionGroup(
    val title: String,
    val subtitle: String,
    val actions: List<CoachPrompt>,
)

private data class CoachTip(
    val icon: String,
    val title: String,
    val body: String,
)

private data class CoachSupportCard(
    val icon: String,
    val title: String,
    val body: String,
)

private val primaryCoachActions = listOf(
    CoachPrompt("How can I adjust this?", "How can I adjust this pattern in a realistic way this week?"),
    CoachPrompt("Tell me more", "Tell me more about the strongest pattern you see right now."),
)

private val coachActionGroups = listOf(
    CoachActionGroup(
        title = "Craving right now",
        subtitle = "Short tactical prompts when you need to delay the next cigarette instead of overthinking it.",
        actions = listOf(
            CoachPrompt("Craving plan", "I have a craving right now and I want a short delay plan."),
            CoachPrompt("Delay next smoke", "Help me delay the next cigarette by at least 15 minutes with a realistic plan."),
        ),
    ),
    CoachActionGroup(
        title = "Stress & reset",
        subtitle = "Use these when the urge is tied to pressure, overload, or the need to decompress.",
        actions = listOf(
            CoachPrompt("Stress reset", "I feel stressed right now and I want a calmer alternative to smoking."),
            CoachPrompt("Break the loop", "What can I do right now to break the automatic loop around this craving?"),
        ),
    ),
    CoachActionGroup(
        title = "Progress & pattern",
        subtitle = "Ask the guide to explain what is improving, what still repeats, and how to adjust this week.",
        actions = listOf(
            CoachPrompt("Progress check", "How am I doing this week, and what seems to be improving?"),
            CoachPrompt("How can I adjust this?", "How can I adjust this pattern in a realistic way this week?"),
            CoachPrompt("Tell me more", "Tell me more about the strongest pattern you see right now."),
        ),
    ),
)

private val coachTips = listOf(
    CoachTip(
        icon = "•",
        title = "The 5-minute delay",
        body = "When a craving hits, set a short timer and wait. Most cravings lose intensity if you break the automatic loop first.",
    ),
    CoachTip(
        icon = "◦",
        title = "Hydration ritual",
        body = "Keep water close. The small ritual helps replace the hand-to-mouth cue and gives the craving a softer landing.",
    ),
)

private fun buildSupportCards(
    messages: List<CoachMessage>,
    hasFallback: Boolean,
): List<CoachSupportCard> {
    val followUps = messages.drop(1)
    if (followUps.isNotEmpty()) {
        return followUps.take(2).mapIndexed { index, message ->
            CoachSupportCard(
                icon = if (index == 0) "!" else "~",
                title = if (index == 0) "Latest follow-up" else "Pattern worth revisiting",
                body = message.body,
            )
        }
    }

    return listOf(
        CoachSupportCard(
            icon = "!",
            title = if (hasFallback) "Fallback mode" else "Trigger identified",
            body = if (hasFallback) {
                "The guide can still help with cravings and delay tactics even when the live model is unavailable."
            } else {
                "Use the guide to identify the routine, stressor, or time window around repeated cigarettes instead of treating each one as isolated."
            },
        ),
        CoachSupportCard(
            icon = "~",
            title = "Best next move",
            body = "When consumption drops on a specific day or routine, protect that context and ask the coach how to repeat it intentionally.",
        ),
    )
}
