package com.feragusper.smokeanalytics.features.chatbot.data

import com.feragusper.smokeanalytics.features.chatbot.domain.CoachContext
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

    override suspend fun sendMessage(message: String, context: CoachContext): String {
        return runCatching {
            gemini.generateContent(buildConversationPrompt(message, context)).text
        }.getOrElse { throwable ->
            throwable.printStackTrace()
            null
        }?.takeIf { it.isNotBlank() } ?: fallbackCoachReply(message, context)
    }

    override suspend fun sendInitialMessage(context: CoachContext): String {
        return runCatching {
            gemini.generateContent(buildInitialPrompt(context)).text
        }.getOrElse { throwable ->
            throwable.printStackTrace()
            null
        }?.takeIf { it.isNotBlank() } ?: fallbackInitialCoachMessage(context)
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
        - Logged smokes in context window: ${context.totalCount}
        - Time since last smoke: ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m
        - Current streak hours: ${context.currentStreakHours}
        - Longest streak hours: ${context.longestStreakHours}

        Write one compact opening message that:
        - explains the coach's purpose in one sentence
        - comments on the user's current pattern
        - gives one concrete next-step suggestion
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
        - Time since last smoke: ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m
        - Current streak hours: ${context.currentStreakHours}
        - Longest streak hours: ${context.longestStreakHours}

        User message:
        $message

        Reply with:
        - empathy first
        - one practical suggestion
        - no fluff
    """.trimIndent()
}
