package com.feragusper.smokeanalytics.features.chatbot.domain

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
        val userMessage = "Hola"
        val expectedReply = "Hola, ¿cómo estás?"
        val smokes = sampleSmokes()

        coEvery { smokeRepository.fetchSmokes() } returns smokes
        coEvery { authRepository.fetchSession() } returns Session.Anonymous
        coEvery { chatbotRepository.sendMessage(userMessage, any()) } returns expectedReply

        val result = useCase.sendMessage(userMessage)

        result shouldBeEqualTo expectedReply
    }

    @Test
    fun `sendInitialMessageWithContext should use displayName from logged-in session`() = runTest {
        val smokes = sampleSmokes(35)
        val session = Session.LoggedIn(
            user = Session.User(
                id = "userId",
                email = "mate@yerba.com",
                displayName = "Fer"
            )
        )

        coEvery { smokeRepository.fetchSmokes() } returns smokes
        coEvery { authRepository.fetchSession() } returns session
        coEvery { chatbotRepository.sendInitialMessage(any()) } returns "¡Hola Fer!"

        val result = useCase.sendInitialMessageWithContext()

        result shouldBeEqualTo "¡Hola Fer!"
    }

    @Test
    fun `sendInitialMessageWithContext should use fallback name if session is anonymous`() =
        runTest {
            val smokes = sampleSmokes(10)

            coEvery { smokeRepository.fetchSmokes() } returns smokes
            coEvery { authRepository.fetchSession() } returns Session.Anonymous
            coEvery { chatbotRepository.sendInitialMessage(any()) } returns "Hola desconocido"

            val result = useCase.sendInitialMessageWithContext()

            result shouldBeEqualTo "Hola desconocido"
        }

    private fun sampleSmokes(count: Int = 5) = List(count) { index ->
        com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke(
            id = "$index",
            date = Clock.System.now().minus(index.toLong() * 45L, DateTimeUnit.MINUTE),
            timeElapsedSincePreviousSmoke = 1L to 0L
        )
    }

}
