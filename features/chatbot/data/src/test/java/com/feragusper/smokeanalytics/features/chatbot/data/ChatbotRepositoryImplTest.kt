package com.feragusper.smokeanalytics.features.chatbot.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

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
        // Given
        val userPrompt = "Hola"
        val responseText = "¡Hola! ¿Cómo estás hoy?"

        val response: GenerateContentResponse = mockk {
            every { text } returns responseText
        }

        coEvery { gemini.generateContent(userPrompt) } returns response

        // When
        val result = repository.sendMessage(userPrompt)

        // Then
        result shouldBeEqualTo responseText
    }

    @Test
    fun `sendMessage should return fallback text when exception is thrown`() = runTest {
        // Given
        val prompt = "Hola"
        coEvery { gemini.generateContent(prompt) } throws RuntimeException("error")

        // When
        val result = repository.sendMessage(prompt)

        // Then
        result shouldBeEqualTo "Ups, el coach tuvo un mal día y no pudo responder."
    }

    @Test
    fun `sendInitialMessageWithContext should build and send correct prompt`() = runTest {
        // Given
        val name = "Fer"
        val now = LocalDateTime.now()
        val smokes = List(10) {
            Smoke(
                id = "$it",
                date = now.minusMinutes(it.toLong()),
                timeElapsedSincePreviousSmoke = 5L to 0L
            )
        }
        val expectedResponse = "¡Vamos Fer! Estás avanzando."

        val response: GenerateContentResponse = mockk {
            every { text } returns expectedResponse
        }

        coEvery { gemini.generateContent(any<String>()) } returns response

        // When
        val result = repository.sendInitialMessageWithContext(name, smokes)

        // Then
        result shouldBeEqualTo expectedResponse
    }

    @Test
    fun `sendInitialMessageWithContext should return fallback text on error`() = runTest {
        // Given
        val name = "Fer"
        val smokes = emptyList<Smoke>()

        coEvery { gemini.generateContent(any<String>()) } throws RuntimeException("fail")

        // When
        val result = repository.sendInitialMessageWithContext(name, smokes)

        // Then
        result shouldBeEqualTo "No se pudo generar un mensaje motivacional. Probá más tarde."
    }
}