package com.feragusper.smokeanalytics.libraries.authentication.domain

import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test

/**
 * Tests for [FetchSessionUseCase] to ensure it correctly handles session fetching logic.
 */
class FetchSessionUseCaseTest {

    private val authenticationRepository: AuthenticationRepository = mockk()
    private val fetchSessionUseCase = FetchSessionUseCase(authenticationRepository)

    /**
     * Verifies that the use case correctly returns an [Session.Anonymous] when the repository indicates
     * that the current session is anonymous.
     */
    @Test
    fun `GIVEN the session is anonymous WHEN invoke is executed THEN it should return anonymous session`() {
        every { authenticationRepository.fetchSession() } answers { Session.Anonymous }

        fetchSessionUseCase() shouldBe Session.Anonymous
    }
}
