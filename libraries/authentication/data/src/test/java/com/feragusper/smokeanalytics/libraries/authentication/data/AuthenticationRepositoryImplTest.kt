package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [AuthenticationRepositoryImpl] to ensure the correct behavior of session management operations.
 */
class AuthenticationRepositoryImplTest {

    private val firebaseAuth: FirebaseAuth = mockk()
    private val authenticationRepository = AuthenticationRepositoryImpl(firebaseAuth)

    /**
     * Verifies that an anonymous session is returned when there is no current user in FirebaseAuth.
     */
    @Test
    fun `GIVEN the user is null WHEN fetchuser is called THEN it should return anonymous session`() {
        every { firebaseAuth.currentUser } returns null

        assertEquals(authenticationRepository.fetchSession(), Session.Anonymous)
    }

    /**
     * Verifies that a logged-in session is returned with the correct user details when a user is present in FirebaseAuth.
     */
    @Test
    fun `GIVEN the user is not null WHEN fetchuser is called THEN it should return a user session`() =
        runTest {
            val displayName = "Fernando Perez"
            val email = "fernancho@gmail.com"
            val id = "123"
            every { firebaseAuth.currentUser } returns mockk<FirebaseUser>().apply {
                every { this@apply.uid } returns id
                every { this@apply.email } returns email
                every { this@apply.displayName } returns displayName
            }

            assertEquals(
                authenticationRepository.fetchSession(),
                Session.LoggedIn(
                    Session.User(
                        id = id,
                        email = email,
                        displayName = displayName
                    )
                ),
            )
        }

}
