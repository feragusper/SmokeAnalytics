package com.feragusper.smokeanalytics.libraries.authentication.domain

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

/**
 * Unit tests for [SignOutUseCase] to ensure the correct behavior of the sign-out operation.
 */
class SignOutUseCaseTest {

    private val authenticationRepository: AuthenticationRepository =
        mockk(relaxed = true) // Create a relaxed mock of AuthenticationRepository
    private val signOutUseCase = SignOutUseCase(authenticationRepository)

    /**
     * Verifies that the use case calls the signOut method of the repository.
     */
    @Test
    fun `GIVEN a valid scenario WHEN invoke is executed THEN signOut should be called`() {
        // Act: invoke the use case
        signOutUseCase()

        // Assert: verify that the signOut method was called on the repository.
        verify { authenticationRepository.signOut() }
    }
}
