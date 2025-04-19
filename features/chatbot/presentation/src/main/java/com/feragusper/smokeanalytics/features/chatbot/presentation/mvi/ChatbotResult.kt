package com.feragusper.smokeanalytics.features.chatbot.presentation.mvi

import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.compose.ChatbotViewState
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

/**
 * Represents the result of processing a [ChatbotIntent].
 */
sealed class ChatbotResult : MVIResult {

    /**
     * Result when a user message has been successfully sent (shown in UI).
     */
    data class UserMessage(val message: ChatbotViewState.Message) : ChatbotResult()

    /**
     * Result when an AI response (coach) has been received.
     */
    data class CoachMessage(val message: ChatbotViewState.Message) : ChatbotResult()

    /**
     * Loading indicator for any pending operation.
     */
    data object Loading : ChatbotResult()

    /**
     * Result when something goes wrong.
     */
    data class Failure(val reason: String) : ChatbotResult()
}