package com.feragusper.smokeanalytics.features.chatbot.domain

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke

interface ChatbotRepository {
    suspend fun sendMessage(message: String): String
    suspend fun sendInitialMessageWithContext(
        name: String,
        recentSmokes: List<Smoke>
    ): String
}
