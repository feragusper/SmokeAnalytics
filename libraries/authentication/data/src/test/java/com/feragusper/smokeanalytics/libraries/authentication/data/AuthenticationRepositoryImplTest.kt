package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Test

class AuthenticationRepositoryImplTest {

    private val firebaseAuth: FirebaseAuth = mockk()
    private val authenticationRepository = AuthenticationRepositoryImpl(firebaseAuth)

    @Test
    fun `GIVEN the user is null WHEN fetchuser is called THEN it should return anonymous session`() {
        every { firebaseAuth.currentUser } returns null

        assertEquals(authenticationRepository.fetchSession(), Session.Anonymous)
    }

    @Test
    fun `GIVEN the user is not null WHEN fetchuser is called THEN it should return a user session`() =
        runTest {
            val displayName = "Fernando Perez"
            val email = "fernancho@gmail.com"
            every { firebaseAuth.currentUser } returns mockk<FirebaseUser>().apply {
                every { this@apply.displayName } returns displayName
                every { this@apply.email } returns email
            }

            assertEquals(
                authenticationRepository.fetchSession(),
                Session.LoggedIn(
                    Session.User(
                        displayName = displayName,
                        email = email
                    )
                ),
            )
        }

}
