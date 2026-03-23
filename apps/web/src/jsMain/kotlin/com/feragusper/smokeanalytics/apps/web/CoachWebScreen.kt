package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotUseCase
import com.feragusper.smokeanalytics.features.chatbot.domain.CoachReplySource
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.GhostButton
import com.feragusper.smokeanalytics.libraries.design.InlineErrorCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun CoachWebScreen(
    chatbotUseCase: ChatbotUseCase,
) {
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

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "AI Coach",
            eyebrow = "The Guide",
            subtitle = "Pattern-aware prompts, quiet fallback support, and a calmer coaching surface built around recent smoking behavior.",
            badgeText = when {
                loading -> "Refreshing"
                hasFallback -> "Fallback guidance"
                else -> "Context-aware"
            },
            actions = {
                GhostButton(
                    text = "Refresh",
                    onClick = { GlobalScope.promise { loadInitialInsight() } },
                    enabled = !loading,
                )
            },
        )

        when {
            loading && primaryInsight == null -> EmptyStateCard(
                title = "Preparing your guide",
                message = "The coach is loading your recent smoking context.",
            )

            primaryInsight == null -> EmptyStateCard(
                title = "No coach insight yet",
                message = "Refresh the coach to load a new insight based on your recent smoking pattern.",
                actionLabel = "Refresh",
                onAction = { GlobalScope.promise { loadInitialInsight() } },
            )

            else -> {
                PrimaryInsightCard(
                    message = primaryInsight,
                    hasFallback = hasFallback,
                    onPrimaryAction = {
                        GlobalScope.promise { sendPrompt(primaryCoachActions.first()) }
                    },
                    onSecondaryAction = {
                        GlobalScope.promise { sendPrompt(primaryCoachActions.last()) }
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

                Div(attrs = { attr("style", "display:flex;flex-wrap:wrap;gap:12px;") }) {
                    coachPrompts.forEach { prompt ->
                        PrimaryButton(
                            text = prompt.label,
                            onClick = { GlobalScope.promise { sendPrompt(prompt) } },
                            enabled = !loading,
                        )
                    }
                }

                Div(attrs = { attr("style", "display:flex;flex-direction:column;gap:12px;") }) {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                        Text("Offline Support & Tips")
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
                    hasFallback = hasFallback,
                    totalInsights = messages.size,
                    totalPrompts = secondaryInsights.size,
                )
            }
        }

        error?.let { message ->
            InlineErrorCard(
                title = "Coach unavailable",
                message = message,
                actionLabel = "Retry",
                onAction = { GlobalScope.promise { loadInitialInsight() } },
            )
        }
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
                    Text("Primary Insight")
                }
            }
            Div(attrs = { attr("style", "font-size:34px;font-weight:800;line-height:1.12;color:var(--sa-color-primary);max-width:640px;") }) {
                Text(message.body)
            }
            Div(attrs = { attr("style", "margin-top:14px;font-size:16px;line-height:1.6;color:var(--sa-color-secondary);max-width:720px;") }) {
                Text(
                    if (hasFallback) {
                        "Fallback guidance is active. The advice still uses recent smoking context, but it is not backed by a live model."
                    } else {
                        "Use the guide to isolate patterns, request a realistic adjustment, and keep follow-ups anchored in your recent behavior."
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
                        Text("Weekly Summary")
                    }
                    Div(attrs = { attr("style", "font-size:32px;font-weight:800;line-height:1.1;margin-top:6px;") }) {
                        Text("Making steady progress")
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

private val coachPrompts = listOf(
    CoachPrompt("Craving plan", "I have a craving right now and I want a short delay plan."),
    CoachPrompt("Progress check", "How am I doing this week, and what seems to be improving?"),
    CoachPrompt("Stress reset", "I feel stressed right now and I want a calmer alternative to smoking."),
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
                title = if (index == 0) "Prompt in focus" else "Pattern worth revisiting",
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
            title = "Weekly shift",
            body = "When consumption drops on a specific day or routine, protect that context and ask the coach how to repeat it intentionally.",
        ),
    )
}
