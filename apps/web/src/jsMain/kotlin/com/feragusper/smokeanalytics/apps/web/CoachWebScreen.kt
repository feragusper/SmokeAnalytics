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
                        title = "Coach insight",
                        body = reply.text,
                        source = reply.source,
                    )
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

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "AI Coach",
            eyebrow = "Insights",
            subtitle = "Short guidance grounded in your recent smoking pattern.",
            badgeText = when {
                loading -> "Refreshing"
                messages.any { it.source == CoachReplySource.Fallback } -> "Fallback guidance"
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

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("How to use it") }
            Div(attrs = { classes(SmokeWebStyles.sectionBody) }) {
                Text("Use the quick prompts to keep the conversation focused on cravings, progress, and delay tactics without turning this into a noisy chat screen.")
            }
            if (messages.any { it.source == CoachReplySource.Fallback }) {
                Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                    Text("Fallback guidance is active on web today. The advice still uses your recent smoking context, but it is not backed by a live model.")
                }
            }
        }

        Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
            coachPrompts.forEach { prompt ->
                PrimaryButton(
                    text = prompt.label,
                    onClick = { GlobalScope.promise { sendPrompt(prompt) } },
                    enabled = !loading,
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

        when {
            loading && messages.isEmpty() -> EmptyStateCard(
                title = "Preparing your insight",
                message = "The coach is loading your recent smoking context.",
            )
            messages.isEmpty() -> EmptyStateCard(
                title = "No coach insight yet",
                message = "Refresh the coach to load a new insight based on your recent smoking pattern.",
                actionLabel = "Refresh",
                onAction = { GlobalScope.promise { loadInitialInsight() } },
            )
            else -> messages.forEach { message ->
                SurfaceCard {
                    Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text(message.title) }
                    Div(attrs = { classes(SmokeWebStyles.sectionBody) }) { Text(message.body) }
                    Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                        Text(
                            if (message.source == CoachReplySource.Live) {
                                "Live model response"
                            } else {
                                "Fallback guidance"
                            }
                        )
                    }
                }
            }
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

private val coachPrompts = listOf(
    CoachPrompt("Craving plan", "I have a craving right now and I want a short delay plan."),
    CoachPrompt("Progress check", "How am I doing this week, and what seems to be improving?"),
    CoachPrompt("Stress reset", "I feel stressed right now and I want a calmer alternative to smoking."),
)
