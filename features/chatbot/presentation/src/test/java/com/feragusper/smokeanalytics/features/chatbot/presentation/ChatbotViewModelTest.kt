package com.feragusper.smokeanalytics.features.chatbot.presentation

import app.cash.turbine.test
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.ChatbotIntent
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.ChatbotResult
import com.feragusper.smokeanalytics.features.chatbot.presentation.mvi.compose.ChatbotViewState
import com.feragusper.smokeanalytics.features.chatbot.presentation.process.ChatbotProcessHolder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatbotViewModelTest {

    private val processHolder: ChatbotProcessHolder = mockk()
    private lateinit var viewModel: ChatbotViewModel

    @BeforeEach
    fun setUp() {
        viewModel = ChatbotViewModel(processHolder)
        viewModel.navigator = mockk(relaxed = true)
    }

    @Test
    fun `should emit loading and coach message when SendInitialMessageWithContext is processed`() =
        runTest {
            // Given
            val reply = "Welcome!"
            val intent = ChatbotIntent.SendInitialMessageWithContext

            every { processHolder.processIntent(intent) } returns flowOf(
                ChatbotResult.Loading,
                ChatbotResult.CoachMessage(ChatbotViewState.Message(reply, isFromUser = false))
            )

            // When & Then
            viewModel.states().test {
                viewModel.intents().trySend(intent)

                awaitItem() shouldBeEqualTo ChatbotViewState() // initial state
                awaitItem() shouldBeEqualTo ChatbotViewState(isLoading = true)
                awaitItem() shouldBeEqualTo ChatbotViewState(
                    messages = listOf(ChatbotViewState.Message(reply, isFromUser = false)),
                    isLoading = false
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `should emit user and coach messages when SendMessage is processed`() = runTest {
        // Given
        val userMessage = "Hola"
        val coachReply = "Hola, ¿cómo estás?"
        val intent = ChatbotIntent.SendMessage(userMessage)

        every { processHolder.processIntent(intent) } returns flowOf(
            ChatbotResult.UserMessage(ChatbotViewState.Message(userMessage, isFromUser = true)),
            ChatbotResult.CoachMessage(ChatbotViewState.Message(coachReply, isFromUser = false))
        )

        // When & Then
        viewModel.states().test {
            viewModel.intents().trySend(intent)

            awaitItem() shouldBeEqualTo ChatbotViewState() // initial state
            awaitItem() shouldBeEqualTo ChatbotViewState(
                messages = listOf(ChatbotViewState.Message(userMessage, isFromUser = true)),
                isLoading = true
            )
            awaitItem() shouldBeEqualTo ChatbotViewState(
                messages = listOf(
                    ChatbotViewState.Message(userMessage, isFromUser = true),
                    ChatbotViewState.Message(coachReply, isFromUser = false)
                ),
                isLoading = false
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should emit failure state`() = runTest {
        // Given
        val error = "no context"
        val intent = ChatbotIntent.SendInitialMessageWithContext

        every { processHolder.processIntent(intent) } returns flowOf(
            ChatbotResult.Loading,
            ChatbotResult.Failure(error)
        )

        // When & Then
        viewModel.states().test {
            viewModel.intents().trySend(intent)

            awaitItem() shouldBeEqualTo ChatbotViewState() // initial state
            awaitItem() shouldBeEqualTo ChatbotViewState(isLoading = true)
            awaitItem() shouldBeEqualTo ChatbotViewState(error = error, isLoading = false)

            cancelAndIgnoreRemainingEvents()
        }
    }
}