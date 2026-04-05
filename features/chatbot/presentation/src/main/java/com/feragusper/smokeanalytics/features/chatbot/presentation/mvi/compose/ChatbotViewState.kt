package com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.chatbot.domain.CoachReplySource
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.ChatbotIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.compose.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.compose.theme.SmokeAnalyticsTheme

data class ChatbotViewState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : MVIViewState<ChatbotIntent> {

    data class Message(
        val text: String,
        val isFromUser: Boolean,
        val source: CoachReplySource = CoachReplySource.Live,
    )

    @Composable
    fun Compose(intent: (ChatbotIntent) -> Unit) {
        LaunchedEffect(messages.isEmpty()) {
            if (messages.isEmpty()) {
                intent(ChatbotIntent.SendInitialMessageWithContext)
            }
        }

        val coachMessages = messages.filterNot { it.isFromUser }
        val primaryInsight = coachMessages.firstOrNull()
        val secondaryInsights = coachMessages.drop(1)
        val hasFallback = coachMessages.any { it.source == CoachReplySource.Fallback }
        val summaryCards = buildSummaryCards(coachMessages = coachMessages, hasFallback = hasFallback)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "The Guide",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Guidance grounded in your smoking rhythm",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "A calmer coaching space for cravings, progress checks, and pattern shifts based on your recent context.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                GuideContextOverview(
                    hasFallback = hasFallback,
                    isLoading = isLoading && primaryInsight == null,
                )
            }

            item {
                PrimaryInsightCard(
                    insight = primaryInsight,
                    isLoading = isLoading && primaryInsight == null,
                    hasFallback = hasFallback,
                    onPrimaryAction = { intent(ChatbotIntent.SendMessage(coachPrimaryActions.first().prompt)) },
                    onSecondaryAction = { intent(ChatbotIntent.SendMessage(coachPrimaryActions.last().prompt)) },
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Ask By Intent",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    coachActionGroups.forEach { group ->
                        ActionGroupCard(
                            group = group,
                            enabled = !isLoading,
                            onActionClick = { prompt ->
                                intent(ChatbotIntent.SendMessage(prompt))
                            },
                        )
                    }
                }
            }

            items(summaryCards) { card ->
                SupportInsightCard(
                    icon = card.icon,
                    title = card.title,
                    body = card.body,
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Quiet Support",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    coachTips.forEach { tip ->
                        TipCard(
                            icon = tip.icon,
                            title = tip.title,
                            body = tip.body,
                        )
                    }
                }
            }

            if (secondaryInsights.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Follow-ups",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                items(secondaryInsights) { message ->
                    FollowUpInsightCard(message = message)
                }
            }

            error?.let { errorMessage ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Guide unavailable",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
            }

            item {
                WeeklySummaryCard(
                    hasFallback = hasFallback,
                    totalCoachMessages = coachMessages.size,
                    totalPromptsUsed = messages.count { it.isFromUser },
                )
            }
        }
    }

    private data class QuickAction(
        val label: String,
        val prompt: String,
    )

    private companion object {
        private val coachPrimaryActions = listOf(
            QuickAction("How can I adjust this?", "How can I adjust this pattern in a realistic way this week?"),
            QuickAction("Tell me more", "Tell me more about the strongest pattern you see right now."),
        )

        private val quickActions = listOf(
            QuickAction("Craving plan", "I have a craving right now and I need a short delay plan."),
            QuickAction("Stress reset", "I feel stressed right now and I want a calmer alternative to smoking."),
            QuickAction("Progress check", "How am I doing this week, and what improved?"),
        )

        private val coachTips = listOf(
            CoachTip(
                icon = "TIP",
                title = "The 5-minute delay",
                body = "When a craving hits, wait five minutes before deciding. Most cravings lose intensity if you interrupt the automatic loop.",
            ),
            CoachTip(
                icon = "H2O",
                title = "Hydration ritual",
                body = "Keep cold water nearby. The small ritual helps with oral fixation and gives the craving a healthier replacement cue.",
            ),
        )
    }
}

@Composable
private fun PrimaryInsightCard(
    insight: ChatbotViewState.Message?,
    isLoading: Boolean,
    hasFallback: Boolean,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "AI",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (hasFallback) "Fallback insight" else "Primary insight",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            when {
                isLoading -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp,
                            )
                        Text(
                            text = "Preparing your guide",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        }
                        Text(
                            text = "Reviewing your recent rhythm, recovery gap, and prompts before the next insight lands.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            LoadingTag(label = "Recent rhythm")
                            LoadingTag(label = "Recovery gap")
                            LoadingTag(label = "Coach context")
                        }
                    }
                }

                insight != null -> {
                    Text(
                        text = insight.text,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                else -> {
                    Text(
                        text = "The coach is waiting for enough context to prepare a focused insight.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Text(
                text = if (hasFallback) {
                    "Live coaching is unavailable right now. The guide is still using your recent smoking context, but the response is coming from the built-in fallback path."
                } else {
                    "Use this insight to decide what to ask next: decode the pattern, get a realistic adjustment, or ask for a short recovery plan."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = onPrimaryAction,
                    label = { Text("How can I adjust this?") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
                AssistChip(
                    onClick = onSecondaryAction,
                    label = { Text("Tell me more") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }
        }
    }
}

@Composable
private fun GuideContextOverview(
    hasFallback: Boolean,
    isLoading: Boolean,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GuideContextCard(
            title = "What it uses",
            body = "Recent smoking rhythm, recovery gaps, and the prompts you ask in this session.",
            accent = "Context",
        )
        GuideContextCard(
            title = "Best asks",
            body = "Craving plans, stress resets, delay tactics, and weekly pattern checks work best here.",
            accent = "Intent",
        )
        GuideContextCard(
            title = if (hasFallback) "Current mode" else "Live mode",
            body = if (isLoading) {
                "Refreshing the next insight now."
            } else if (hasFallback) {
                "Fallback guidance is active, so answers stay practical and bounded even without the live model."
            } else {
                "Live coaching is active, so the guide can respond with more tailored follow-ups."
            },
            accent = if (hasFallback) "Fallback" else "Live",
        )
    }
}

@Composable
private fun GuideContextCard(
    title: String,
    body: String,
    accent: String,
) {
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = accent,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LoadingTag(
    label: String,
) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun ActionGroupCard(
    group: CoachActionGroup,
    enabled: Boolean,
    onActionClick: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = group.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                group.actions.forEach { action ->
                    AssistChip(
                        onClick = { onActionClick(action.prompt) },
                        enabled = enabled,
                        label = { Text(action.label) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (action.emphasized) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            labelColor = if (action.emphasized) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportInsightCard(
    icon: String,
    title: String,
    body: String,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TipCard(
    icon: String,
    title: String,
    body: String,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(16.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FollowUpInsightCard(
    message: ChatbotViewState.Message,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (message.source == CoachReplySource.Live) "Context-aware reply" else "Fallback guidance",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (message.source == CoachReplySource.Live) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun WeeklySummaryCard(
    hasFallback: Boolean,
    totalCoachMessages: Int,
    totalPromptsUsed: Int,
) {
    Card(
        modifier = Modifier.padding(bottom = 16.dp),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer,
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Guide Snapshot",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                    )
                    Text(
                        text = if (hasFallback) "Steady fallback support" else "Live coaching session",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Text(
                    text = "SHIFT",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.48f),
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryMetric(
                    title = "Guide mode",
                    value = if (hasFallback) "Fallback" else "Live",
                )
                SummaryMetric(
                    title = "Prompts used",
                    value = totalPromptsUsed.toString(),
                )
                SummaryMetric(
                    title = "Insights",
                    value = totalCoachMessages.toString(),
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    title: String,
    value: String,
) {
    Card(
        modifier = Modifier.width(152.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

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

private data class CoachAction(
    val label: String,
    val prompt: String,
    val emphasized: Boolean = false,
)

private data class CoachActionGroup(
    val title: String,
    val subtitle: String,
    val actions: List<CoachAction>,
)

private val coachActionGroups = listOf(
    CoachActionGroup(
        title = "Craving right now",
        subtitle = "Short tactical prompts when you need to delay the next cigarette instead of overthinking it.",
        actions = listOf(
            CoachAction(
                label = "Craving plan",
                prompt = "I have a craving right now and I need a short delay plan.",
                emphasized = true,
            ),
            CoachAction(
                label = "Delay next smoke",
                prompt = "Help me delay the next cigarette by at least 15 minutes with a realistic plan.",
            ),
        ),
    ),
    CoachActionGroup(
        title = "Stress & reset",
        subtitle = "Use these when the urge is tied to pressure, overload, or the need to decompress.",
        actions = listOf(
            CoachAction(
                label = "Stress reset",
                prompt = "I feel stressed right now and I want a calmer alternative to smoking.",
                emphasized = true,
            ),
            CoachAction(
                label = "Break the loop",
                prompt = "What can I do right now to break the automatic loop around this craving?",
            ),
        ),
    ),
    CoachActionGroup(
        title = "Progress & pattern",
        subtitle = "Ask the guide to explain what is improving, what still repeats, and how to adjust this week.",
        actions = listOf(
            CoachAction(
                label = "Progress check",
                prompt = "How am I doing this week, and what improved?",
                emphasized = true,
            ),
            CoachAction(
                label = "How can I adjust this?",
                prompt = "How can I adjust this pattern in a realistic way this week?",
            ),
            CoachAction(
                label = "Tell me more",
                prompt = "Tell me more about the strongest pattern you see right now.",
            ),
        ),
    ),
)

private fun buildSummaryCards(
    coachMessages: List<ChatbotViewState.Message>,
    hasFallback: Boolean,
): List<CoachSupportCard> {
    val promptResponses = coachMessages.drop(1)
    if (promptResponses.isNotEmpty()) {
        return promptResponses.take(2).mapIndexed { index, message ->
            CoachSupportCard(
                icon = if (index == 0) "NOW" else "PATTERN",
                title = if (index == 0) "Latest follow-up" else "Pattern worth revisiting",
                body = message.text,
            )
        }
    }

    return listOf(
        CoachSupportCard(
            icon = if (hasFallback) "SAFE" else "TRIGGER",
            title = if (hasFallback) "Fallback mode" else "Trigger identified",
            body = if (hasFallback) {
                "The guide can still help with cravings and delay tactics even when the live model is unavailable."
            } else {
                "Use the guide when a recurring craving window starts to show up. Focus on the routine around it, not only the cigarette itself."
            },
        ),
        CoachSupportCard(
            icon = "SHIFT",
            title = "Best next move",
            body = "Use progress checks to spot when consumption drops, then protect the environment or routine that helped create that change.",
        ),
    )
}

@CombinedPreviews
@Composable
private fun ChatbotViewPreview() {
    SmokeAnalyticsTheme {
        ChatbotViewState().Compose {}
    }
}
