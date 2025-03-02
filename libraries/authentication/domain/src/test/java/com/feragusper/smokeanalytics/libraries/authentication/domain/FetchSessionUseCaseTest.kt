package com.feragusper.smokeanalytics.libraries.authentication.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

/**
 * Unit tests for [FetchSessionUseCase] to ensure the correct behavior of session fetching logic.
 */
class FetchSessionUseCaseTest {

    private val authenticationRepository: AuthenticationRepository =
        mockk(relaxed = true) // Create a mock of AuthenticationRepository
    private val fetchSessionUseCase = FetchSessionUseCase(authenticationRepository)

    /**
     * Verifies that the use case correctly returns an [Session.Anonymous] when the repository indicates
     * that the current session is anonymous.
     */
    @Test
    fun `GIVEN the session is anonymous WHEN invoke is executed THEN it should return anonymous session`() {
        // Mock the repository to return an anonymous session
        every { authenticationRepository.fetchSession() } returns Session.Anonymous

        // Assert that the use case correctly returns an anonymous session.
        fetchSessionUseCase().shouldBeEqualTo(Session.Anonymous)
    }

    /**
     * Verifies that the use case correctly returns a [Session.LoggedIn] session with the correct user details
     * when the repository indicates that the session is not anonymous.
     */
    @Test
    fun `GIVEN the session is logged in WHEN invoke is executed THEN it should return a logged-in session with user details`() {
        val userId = "123"
        val userEmail = "user@example.com"
        val userDisplayName = "John Doe"

        // Mock the repository to return a logged-in session with mock user details.
        every { authenticationRepository.fetchSession() } returns Session.LoggedIn(
            Session.User(
                userId,
                userEmail,
                userDisplayName
            )
        )

        // Create the expected result
        val expectedSession = Session.LoggedIn(Session.User(userId, userEmail, userDisplayName))

        // Assert that the returned session matches the expected session using `shouldBeEqualTo` for value equality.
        fetchSessionUseCase().shouldBeEqualTo(expectedSession)
    }

    /**
     * Verifies that the use case calls the repository's fetchSession method.
     */
    @Test
    fun `GIVEN any scenario WHEN invoke is executed THEN fetchSession should be called`() {
        // Act: invoke the use case
        fetchSessionUseCase()

        // Assert: verify that fetchSession was called on the repository.
        verify { authenticationRepository.fetchSession() }
    }
}