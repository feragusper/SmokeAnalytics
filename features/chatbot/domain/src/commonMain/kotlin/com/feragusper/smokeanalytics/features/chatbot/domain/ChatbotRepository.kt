package com.feragusper.smokeanalytics.features.chatbot.domain

interface ChatbotRepository {
    suspend fun sendMessage(
        message: String,
        context: CoachContext,
    ): String

    suspend fun sendInitialMessage(
        context: CoachContext,
    ): String
}
