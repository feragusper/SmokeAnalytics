package com.feragusper.smokeanalytics.features.chatbot.data

import com.feragusper.smokeanalytics.features.chatbot.domain.CoachContext
import com.feragusper.smokeanalytics.features.chatbot.domain.CoachReply
import com.feragusper.smokeanalytics.features.chatbot.domain.CoachReplySource
import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotRepository
import com.feragusper.smokeanalytics.features.chatbot.domain.fallbackCoachReply
import com.feragusper.smokeanalytics.features.chatbot.domain.fallbackInitialCoachMessage
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatbotRepositoryImpl @Inject constructor(
    private val gemini: GenerativeModel,
) : ChatbotRepository {

    override suspend fun sendMessage(message: String, context: CoachContext): CoachReply {
        return runCatching {
            gemini.generateContent(buildConversationPrompt(message, context)).text
        }.getOrElse { throwable ->
            throwable.printStackTrace()
            null
        }?.takeIf { it.isNotBlank() }?.let {
            CoachReply(
                text = it,
                source = CoachReplySource.Live,
            )
        } ?: CoachReply(
            text = fallbackCoachReply(message, context),
            source = CoachReplySource.Fallback,
        )
    }

    override suspend fun sendInitialMessage(context: CoachContext): CoachReply {
        return runCatching {
            gemini.generateContent(buildInitialPrompt(context)).text
        }.getOrElse { throwable ->
            throwable.printStackTrace()
            null
        }?.takeIf { it.isNotBlank() }?.let {
            CoachReply(
                text = it,
                source = CoachReplySource.Live,
            )
        } ?: CoachReply(
            text = fallbackInitialCoachMessage(context),
            source = CoachReplySource.Fallback,
        )
    }

    private fun buildInitialPrompt(
        context: CoachContext,
    ): String = """
        You are Smoke Analytics Coach.
        Your role is to help the user reduce smoking through calm, concrete, non-judgmental advice.
        Keep the response short, practical, and human. Avoid cheesy lines, therapy language, and long disclaimers.

        User:
        - Name: ${context.name}
        - Smokes today: ${context.todayCount}
        - Smokes this week: ${context.weekCount}
        - Smokes this month: ${context.monthCount}
        - Logged smokes in context window: ${context.totalCount}
        - Time since last smoke: ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m
        - Current streak hours: ${context.currentStreakHours}
        - Longest streak hours: ${context.longestStreakHours}
        - Average gap between recent smokes: ${context.averageGapMinutes} minutes

        Write one compact opening message that:
        - explains the coach's purpose in one short sentence
        - comments on the user's current pattern using the provided data
        - gives one concrete next-step suggestion
        - stays below 80 words
    """.trimIndent()

    private fun buildConversationPrompt(
        message: String,
        context: CoachContext,
    ): String = """
        You are Smoke Analytics Coach.
        Keep answers short, specific, and actionable.
        Focus on helping the user delay, reduce, or better understand smoking triggers.

        User context:
        - Name: ${context.name}
        - Smokes today: ${context.todayCount}
        - Smokes this week: ${context.weekCount}
        - Smokes this month: ${context.monthCount}
        - Time since last smoke: ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m
        - Current streak hours: ${context.currentStreakHours}
        - Longest streak hours: ${context.longestStreakHours}
        - Average gap between recent smokes: ${context.averageGapMinutes} minutes

        User message:
        $message

        Reply with:
        - empathy first
        - one practical suggestion grounded in the user's data
        - no fluff
        - under 120 words
    """.trimIndent()
}
