package com.feragusper.smokeanalytics.apps.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotUseCase
import com.feragusper.smokeanalytics.libraries.design.EmptyStateCard
import com.feragusper.smokeanalytics.libraries.design.InlineErrorCard
import com.feragusper.smokeanalytics.libraries.design.PageSectionHeader
import com.feragusper.smokeanalytics.libraries.design.PrimaryButton
import com.feragusper.smokeanalytics.libraries.design.SmokeWebStyles
import com.feragusper.smokeanalytics.libraries.design.SurfaceCard
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea

private data class CoachMessage(
    val text: String,
    val fromUser: Boolean,
)

@Composable
fun CoachWebScreen(
    chatbotUseCase: ChatbotUseCase,
) {
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<CoachMessage>>(emptyList()) }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(chatbotUseCase) {
        loading = true
        error = null
        runCatching { chatbotUseCase.sendInitialMessageWithContext() }
            .onSuccess { reply -> messages = listOf(CoachMessage(reply, fromUser = false)) }
            .onFailure { throwable -> error = throwable.message ?: "Coach unavailable right now." }
        loading = false
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || loading) return
        val trimmed = text.trim()
        messages = messages + CoachMessage(trimmed, fromUser = true)
        input = ""
        loading = true
        error = null
        scope.launch {
            runCatching { chatbotUseCase.sendMessage(trimmed) }
                .onSuccess { reply ->
                    messages = messages + CoachMessage(reply, fromUser = false)
                }
                .onFailure { throwable ->
                    error = throwable.message ?: "Coach unavailable right now."
                }
            loading = false
        }
    }

    Div(attrs = { classes(SmokeWebStyles.panelStack) }) {
        PageSectionHeader(
            title = "Coach",
            eyebrow = "Support",
            badgeText = if (loading) "Thinking" else "Ready",
            actions = {
                PrimaryButton(
                    text = "New prompt",
                    onClick = {
                        messages = emptyList()
                        input = ""
                        error = null
                        scope.launch {
                            loading = true
                            runCatching { chatbotUseCase.sendInitialMessageWithContext() }
                                .onSuccess { reply -> messages = listOf(CoachMessage(reply, fromUser = false)) }
                                .onFailure { throwable -> error = throwable.message ?: "Coach unavailable right now." }
                            loading = false
                        }
                    },
                    enabled = !loading,
                )
            }
        )

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("What it's for") }
            Div(attrs = { classes(SmokeWebStyles.helperText) }) {
                Text("Use the coach for cravings, pattern checks, and practical ways to delay the next cigarette.")
            }
        }

        error?.let { message ->
            InlineErrorCard(
                title = "Coach unavailable",
                message = message,
            )
        }

        if (messages.isEmpty() && !loading && error == null) {
            EmptyStateCard(
                title = "No coaching message yet",
                message = "Start with a quick prompt and the coach will answer using your latest smoking context.",
            )
        } else {
            messages.forEach { message ->
                if (message.fromUser) {
                    SurfaceCard(SmokeWebStyles.elapsedCardCalm) {
                        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                            Text("You")
                        }
                        Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(message.text) }
                    }
                } else {
                    SurfaceCard {
                        Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) {
                            Text("Coach")
                        }
                        Div(attrs = { classes(SmokeWebStyles.helperText) }) { Text(message.text) }
                    }
                }
            }
        }

        SurfaceCard {
            Div(attrs = { classes(SmokeWebStyles.sectionTitle) }) { Text("Ask the coach") }
            TextArea(
                value = input,
                attrs = {
                    attr("rows", "4")
                    attr("placeholder", "I'm craving now. Help me delay the next one.")
                    attr("style", "width:100%;box-sizing:border-box;margin-top:12px;padding:14px;border-radius:16px;border:1px solid var(--sa-color-outline);background:var(--sa-color-surface-strong);font:inherit;resize:vertical;")
                    if (loading) disabled()
                    onInput { input = it.value }
                }
            )

            Div(attrs = { classes(SmokeWebStyles.sectionActions) }) {
                PrimaryButton(
                    text = if (loading) "Sending..." else "Send",
                    onClick = { sendMessage(input) },
                    enabled = input.isNotBlank() && !loading,
                )
                PrimaryButton(
                    text = "Craving",
                    onClick = { sendMessage("I'm craving right now. Help me delay the next cigarette.") },
                    enabled = !loading,
                )
                PrimaryButton(
                    text = "Progress",
                    onClick = { sendMessage("How am I doing today?") },
                    enabled = !loading,
                )
            }
        }
    }
}
