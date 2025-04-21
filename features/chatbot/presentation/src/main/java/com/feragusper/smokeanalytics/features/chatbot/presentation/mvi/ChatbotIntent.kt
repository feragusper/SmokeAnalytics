package com.feragusper.smokeanalytics.features.chatbot.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

/**
 * Defines user intentions that can trigger actions within the Chatbot (coach) feature.
 */
sealed class ChatbotIntent : MVIIntent {

    /**
     * Sends an initial message to the coach with context from the user's smoking data.
     */
    data object SendInitialMessageWithContext : ChatbotIntent()

    /**
     * Sends a follow-up user message to the coach for ongoing conversation.
     *
     * @param text The message the user wants to send.
     */
    data class SendMessage(val text: String) : ChatbotIntent()
}