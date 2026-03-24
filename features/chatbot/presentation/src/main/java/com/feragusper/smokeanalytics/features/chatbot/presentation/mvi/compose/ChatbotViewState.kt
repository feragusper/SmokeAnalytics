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
                        text = "Reflecting on your week",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Short, grounded guidance based on your recent smoking pattern and recovery rhythm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    quickActions.forEach { action ->
                        AssistChip(
                            onClick = { intent(ChatbotIntent.SendMessage(action.prompt)) },
                            label = { Text(action.label) },
                            enabled = !isLoading,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
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
                        text = "Offline Support & Tips",
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
                                text = "Coach unavailable",
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
                    text = "Primary Insight",
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
                    "Fallback guidance is active. The advice still uses your recent smoking context, but it is not backed by a live model."
                } else {
                    "Use the guide for cravings, progress checks, and practical pattern shifts without turning it into a noisy chat screen."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                coachActionPills.forEach { action ->
                    AssistChip(
                        onClick = if (action.primary) onPrimaryAction else onSecondaryAction,
                        label = { Text(action.label) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (action.primary) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            labelColor = if (action.primary) {
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
                        text = "Weekly Summary",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                    )
                    Text(
                        text = "Making steady progress",
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

private data class CoachActionPill(
    val label: String,
    val primary: Boolean,
)

private val coachActionPills = listOf(
    CoachActionPill(label = "How can I adjust this?", primary = true),
    CoachActionPill(label = "Tell me more", primary = false),
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
                title = if (index == 0) "Prompt in focus" else "Pattern worth revisiting",
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
            title = "Weekly shift",
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
