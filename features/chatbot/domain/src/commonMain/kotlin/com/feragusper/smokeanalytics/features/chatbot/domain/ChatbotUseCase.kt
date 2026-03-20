package com.feragusper.smokeanalytics.features.chatbot.domain

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository

class ChatbotUseCase constructor(
    private val smokeRepository: SmokeRepository,
    private val authRepository: AuthenticationRepository,
    private val chatbotRepository: ChatbotRepository
) {

    suspend fun sendMessage(message: String): CoachReply {
        val context = loadContext()
        return chatbotRepository.sendMessage(message, context)
    }

    suspend fun sendInitialMessageWithContext(): CoachReply {
        return chatbotRepository.sendInitialMessage(loadContext())
    }

    private suspend fun loadContext(): CoachContext {
        val smokes = smokeRepository.fetchSmokes().take(30)
        val name = (authRepository.fetchSession() as? Session.LoggedIn)?.user?.displayName
            ?: "Smoker"
        return buildCoachContext(name = name, recentSmokes = smokes)
    }
}
