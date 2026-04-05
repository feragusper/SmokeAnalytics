package com.feragusper.smokeanalytics.features.chatbot.data

import com.feragusper.smokeanalytics.features.chatbot.domain.CoachContext
import com.feragusper.smokeanalytics.features.chatbot.domain.CoachReply
import com.feragusper.smokeanalytics.features.chatbot.domain.CoachReplySource
import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotRepository
import com.feragusper.smokeanalytics.features.chatbot.domain.buildConversationPrompt
import com.feragusper.smokeanalytics.features.chatbot.domain.buildInitialCoachPrompt
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
    ): String = buildInitialCoachPrompt(context)

    private fun buildConversationPrompt(
        message: String,
        context: CoachContext,
    ): String = buildConversationPrompt(message, context)
}
