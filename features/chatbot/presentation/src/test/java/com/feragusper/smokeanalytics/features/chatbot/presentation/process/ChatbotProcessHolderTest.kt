package com.feragusper.smokeanalytics.features.chatbot.presentation.process

import com.feragusper.smokeanalytics.features.chatbot.domain.ChatbotUseCase
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.ChatbotIntent
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.ChatbotResult
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.compose.ChatbotViewState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatbotProcessHolderTest {

    private val chatbotUseCase: ChatbotUseCase = mockk()
    private lateinit var processHolder: ChatbotProcessHolder

    @BeforeEach
    fun setUp() {
        processHolder = ChatbotProcessHolder(chatbotUseCase)
    }

    @Test
    fun `should emit user and coach messages when SendMessage intent is processed`() = runTest {
        // Given
        val userMessage = "I need help"
        val coachReply = "Of course, I'm here for you"
        coEvery { chatbotUseCase.sendMessage(userMessage) } returns coachReply

        // When
        val results = processHolder.processIntent(ChatbotIntent.SendMessage(userMessage)).toList()

        // Then
        results shouldBeEqualTo listOf(
            ChatbotResult.UserMessage(ChatbotViewState.Message(userMessage, isFromUser = true)),
            ChatbotResult.CoachMessage(ChatbotViewState.Message(coachReply, isFromUser = false))
        )
    }

    @Test
    fun `should emit loading and coach message when SendInitialMessageWithContext intent is processed`() =
        runTest {
            // Given
            val reply = "Let's talk about your progress"
            coEvery { chatbotUseCase.sendInitialMessageWithContext() } returns reply

            // When
            val results =
                processHolder.processIntent(ChatbotIntent.SendInitialMessageWithContext).toList()

            // Then
            results shouldBeEqualTo listOf(
                ChatbotResult.Loading,
                ChatbotResult.CoachMessage(ChatbotViewState.Message(reply, isFromUser = false))
            )
        }

    @Test
    fun `should emit failure when sendMessage throws an exception`() = runTest {
        // Given
        val userMessage = "I'm stressed"
        coEvery { chatbotUseCase.sendMessage(userMessage) } throws RuntimeException("Boom")

        // When
        val results = processHolder.processIntent(ChatbotIntent.SendMessage(userMessage)).toList()

        // Then
        results shouldBeEqualTo listOf(
            ChatbotResult.UserMessage(ChatbotViewState.Message(userMessage, isFromUser = true)),
            ChatbotResult.Failure("Boom")
        )
    }

    @Test
    fun `should emit failure when sendInitialMessageWithContext throws an exception`() = runTest {
        // Given
        coEvery { chatbotUseCase.sendInitialMessageWithContext() } throws RuntimeException("Context failed")

        // When
        val results =
            processHolder.processIntent(ChatbotIntent.SendInitialMessageWithContext).toList()

        // Then
        results shouldBeEqualTo listOf(
            ChatbotResult.Loading,
            ChatbotResult.Failure("Context failed")
        )
    }
}
