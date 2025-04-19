package com.feragusper.smokeanalytics.features.chatbot.presentation

import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.ChatbotIntent
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.ChatbotResult
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.compose.ChatbotViewState
import com.feragusper.smokeanalytics.features.chatbot.presentation.navigation.ChatbotNavigator
import com.feragusper.smokeanalytics.features.chatbot.presentation.process.ChatbotProcessHolder
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val processHolder: ChatbotProcessHolder
) : MVIViewModel<ChatbotIntent, ChatbotViewState, ChatbotResult, ChatbotNavigator>(
    initialState = ChatbotViewState()
) {

    override lateinit var navigator: ChatbotNavigator

    override fun transformer(intent: ChatbotIntent) = processHolder.processIntent(intent)

    override fun reducer(
        previous: ChatbotViewState,
        result: ChatbotResult
    ): ChatbotViewState = when (result) {

        is ChatbotResult.UserMessage -> previous.copy(
            messages = previous.messages + result.message,
            isLoading = true
        )

        is ChatbotResult.CoachMessage -> previous.copy(
            messages = previous.messages + result.message,
            isLoading = false
        )

        is ChatbotResult.Loading -> previous.copy(isLoading = true)

        is ChatbotResult.Failure -> previous.copy(
            error = result.reason,
            isLoading = false
        )
    }
}