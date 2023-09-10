package com.feragusper.smokeanalytics.libraries.authentication.domain

import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Test

class FetchSessionUseCaseTest {

    private val authenticationRepository: AuthenticationRepository = mockk()
    private val fetchSessionUseCase = FetchSessionUseCase(authenticationRepository)

    @Test
    fun `GIVEN the session is anonymous WHEN invoke is executed THEN it should return anonymous session`() {
        every { authenticationRepository.fetchSession() } answers { Session.Anonymous }

        assertEquals(fetchSessionUseCase(), Session.Anonymous)
    }
}
