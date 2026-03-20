package com.feragusper.smokeanalytics.features.chatbot.domain

enum class CoachReplySource {
    Live,
    Fallback,
}

data class CoachReply(
    val text: String,
    val source: CoachReplySource,
)

interface ChatbotRepository {
    suspend fun sendMessage(
        message: String,
        context: CoachContext,
    ): CoachReply

    suspend fun sendInitialMessage(
        context: CoachContext,
    ): CoachReply
}
