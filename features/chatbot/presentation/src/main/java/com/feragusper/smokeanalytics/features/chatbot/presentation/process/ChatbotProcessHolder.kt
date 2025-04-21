package com.feragusper.smokeanalytics.features.chatbot.presentation.process

import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotUseCase
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.ChatbotIntent
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.ChatbotResult
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.compose.ChatbotViewState
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.catchAndLog
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ChatbotProcessHolder @Inject constructor(
    private val chatbotUseCase: ChatbotUseCase
) : MVIProcessHolder<ChatbotIntent, ChatbotResult> {

    override fun processIntent(intent: ChatbotIntent): Flow<ChatbotResult> {
        return when (intent) {
            is ChatbotIntent.SendMessage -> sendMessage(intent.text)
            is ChatbotIntent.SendInitialMessageWithContext -> sendInitialContextualMessage()
        }
    }

    private fun sendMessage(userText: String): Flow<ChatbotResult> = flow {
        emit(ChatbotResult.UserMessage(ChatbotViewState.Message(userText, isFromUser = true)))

        val reply = chatbotUseCase.sendMessage(userText)

        emit(ChatbotResult.CoachMessage(ChatbotViewState.Message(reply, isFromUser = false)))
    }.catchAndLog { e ->
        emit(ChatbotResult.Failure(e.message ?: "Unknown error"))
    }

    private fun sendInitialContextualMessage(): Flow<ChatbotResult> = flow {
        emit(ChatbotResult.Loading)

        val reply = chatbotUseCase.sendInitialMessageWithContext()

        emit(ChatbotResult.CoachMessage(ChatbotViewState.Message(reply, isFromUser = false)))
    }.catchAndLog { e ->
        emit(ChatbotResult.Failure(e.message ?: "Unknown error"))
    }
}