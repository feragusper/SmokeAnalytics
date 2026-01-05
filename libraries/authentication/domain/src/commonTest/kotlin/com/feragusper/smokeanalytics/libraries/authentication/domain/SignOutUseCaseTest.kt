package com.feragusper.smokeanalytics.libraries.authentication.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SignOutUseCaseTest {

    private class FakeAuthenticationRepository : AuthenticationRepository {

        var signOutCalled = false

        override fun fetchSession(): Session = Session.Anonymous

        override suspend fun signOut() {
            signOutCalled = true
        }
    }

    @Test
    fun `WHEN invoke is executed THEN signOut is called`() = runTest {
        val repository = FakeAuthenticationRepository()
        val useCase = SignOutUseCase(repository)

        useCase()

        assertTrue(repository.signOutCalled)
    }
}