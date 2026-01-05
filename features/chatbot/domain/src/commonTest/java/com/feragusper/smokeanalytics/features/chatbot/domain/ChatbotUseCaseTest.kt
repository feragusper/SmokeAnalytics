package com.feragusper.smokeanalytics.features.chatbot.domain

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ChatbotUseCaseTest {

    private val smokeRepository: SmokeRepository = mockk()
    private val authRepository: AuthenticationRepository = mockk()
    private val chatbotRepository: ChatbotRepository = mockk()

    private lateinit var useCase: ChatbotUseCase

    @BeforeEach
    fun setUp() {
        useCase = ChatbotUseCase(smokeRepository, authRepository, chatbotRepository)
    }

    @Test
    fun `sendMessage should delegate to chatbotRepository`() = runTest {
        // Given
        val userMessage = "Hola"
        val expectedReply = "Hola, ¿cómo estás?"

        coEvery { chatbotRepository.sendMessage(userMessage) } returns expectedReply

        // When
        val result = useCase.sendMessage(userMessage)

        // Then
        result shouldBeEqualTo expectedReply
    }

    @Test
    fun `sendInitialMessageWithContext should use displayName from logged-in session`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val smokes = List(35) { index ->
            Smoke(
                id = "$index",
                date = now.minusMinutes(index.toLong()),
                timeElapsedSincePreviousSmoke = 5L to 0L
            )
        }
        val session = Session.LoggedIn(
            user = Session.User(
                id = "userId",
                email = "mate@yerba.com",
                displayName = "Fer"
            )
        )

        coEvery { smokeRepository.fetchSmokes() } returns smokes
        coEvery { authRepository.fetchSession() } returns session
        coEvery {
            chatbotRepository.sendInitialMessageWithContext("Fer", smokes.take(30))
        } returns "¡Hola Fer!"

        // When
        val result = useCase.sendInitialMessageWithContext()

        // Then
        result shouldBeEqualTo "¡Hola Fer!"
    }

    @Test
    fun `sendInitialMessageWithContext should use fallback name if session is anonymous`() =
        runTest {
            // Given
            val now = LocalDateTime.now()
            val smokes = List(10) {
                Smoke(
                    id = "$it",
                    date = now.minusMinutes(it.toLong()),
                    timeElapsedSincePreviousSmoke = 10L to 0L
                )
            }

            coEvery { smokeRepository.fetchSmokes() } returns smokes
            coEvery { authRepository.fetchSession() } returns Session.Anonymous
            coEvery {
                chatbotRepository.sendInitialMessageWithContext("Usuario sin nombre", smokes)
            } returns "Hola desconocido"

            // When
            val result = useCase.sendInitialMessageWithContext()

            // Then
            result shouldBeEqualTo "Hola desconocido"
        }

}
