package com.feragusper.smokeanalytics.features.chatbot.domain

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import javax.inject.Inject

class ChatbotUseCase @Inject constructor(
    private val smokeRepository: SmokeRepository,
    private val authRepository: AuthenticationRepository,
    private val chatbotRepository: ChatbotRepository
) {

    suspend fun sendMessage(message: String): String {
        return chatbotRepository.sendMessage(message)
    }

    suspend fun sendInitialMessageWithContext(): String {
        val smokes = smokeRepository.fetchSmokes().take(30)
        val name = (authRepository.fetchSession() as? Session.LoggedIn)?.user?.displayName
            ?: "Usuario sin nombre"
        return chatbotRepository.sendInitialMessageWithContext(name, smokes)
    }
}
