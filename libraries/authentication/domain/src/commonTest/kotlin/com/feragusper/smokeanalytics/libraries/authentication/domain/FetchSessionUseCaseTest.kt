package com.feragusper.smokeanalytics.libraries.authentication.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class FetchSessionUseCaseTest {

    private class FakeAuthenticationRepository(
        private val session: Session
    ) : AuthenticationRepository {

        override fun fetchSession(): Session = session

        override suspend fun signOut() {
            // no-op
        }
    }

    @Test
    fun `returns Anonymous when repository returns Anonymous`() {
        val repo = FakeAuthenticationRepository(Session.Anonymous)
        val useCase = FetchSessionUseCase(repo)

        val result = useCase()

        assertEquals(Session.Anonymous, result)
    }

    @Test
    fun `returns LoggedIn when repository returns LoggedIn`() {
        val user = Session.User(
            id = "123",
            email = "user@test.com",
            displayName = "Fer"
        )
        val repo = FakeAuthenticationRepository(Session.LoggedIn(user))
        val useCase = FetchSessionUseCase(repo)

        val result = useCase()

        assertEquals(Session.LoggedIn(user), result)
    }
}