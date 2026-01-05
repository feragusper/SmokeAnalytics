package com.feragusper.smokeanalytics.features.chatbot.data

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.minus
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatbotRepositoryImplTest {

    private val gemini: GenerativeModel = mockk()
    private lateinit var repository: ChatbotRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = ChatbotRepositoryImpl(gemini)
    }

    @Test
    fun `sendMessage should return generated text from Gemini`() = runTest {
        val userPrompt = "Hola"
        val responseText = "¡Hola! ¿Cómo estás hoy?"

        val response: GenerateContentResponse = mockk {
            every { text } returns responseText
        }

        coEvery { gemini.generateContent(userPrompt) } returns response

        val result = repository.sendMessage(userPrompt)

        result shouldBeEqualTo responseText
    }

    @Test
    fun `sendInitialMessageWithContext should build and send correct prompt`() = runTest {
        val name = "Fer"
        val now: Instant = Clock.System.now()

        val smokes = List(10) { index ->
            Smoke(
                id = "$index",
                date = now.minus(index.toLong(), DateTimeUnit.MINUTE),
                timeElapsedSincePreviousSmoke = 5L to 0L
            )
        }

        val expectedResponse = "¡Vamos Fer! Estás avanzando."

        val response: GenerateContentResponse = mockk {
            every { text } returns expectedResponse
        }

        coEvery { gemini.generateContent(any<String>()) } returns response

        val result = repository.sendInitialMessageWithContext(name, smokes)

        result shouldBeEqualTo expectedResponse
    }

}