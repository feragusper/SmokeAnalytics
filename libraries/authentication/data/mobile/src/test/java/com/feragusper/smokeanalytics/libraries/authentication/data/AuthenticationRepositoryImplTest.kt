package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
    fun `GIVEN the user is null WHEN fetchSession is called THEN it should return anonymous session`() {
        every { firebaseAuth.currentUser } returns null

        val session = authenticationRepository.fetchSession()

        session shouldBe Session.Anonymous
    }

    /**
     * Verifies that a logged-in session is returned with the correct user details when a user is present in FirebaseAuth.
     */
    @Test
    fun `GIVEN the user is not null WHEN fetchSession is called THEN it should return a user session`() {
        val displayName = "Fernando Perez"
        val email = "fernancho@gmail.com"
        val id = "123"
        val firebaseUser: FirebaseUser = mockk()

        every { firebaseUser.uid } returns id
        every { firebaseUser.email } returns email
        every { firebaseUser.displayName } returns displayName
        every { firebaseAuth.currentUser } returns firebaseUser

        val session = authenticationRepository.fetchSession()

        session shouldBeEqualTo Session.LoggedIn(Session.User(id, email, displayName))
    }

    /**
     * Verifies that the signOut method correctly calls the signOut function from FirebaseAuth.
     */
    @Test
    fun `GIVEN signOut is called WHEN signOut is invoked THEN it should call firebaseAuth signOut`() =
        runTest {
            // Arrange: Set up a mock for firebaseAuth and ensure signOut is called
            every { firebaseAuth.signOut() } returns Unit

            // Act: Call the signOut method
            authenticationRepository.signOut()

            // Assert: Verify that the signOut method was called
            verify { firebaseAuth.signOut() }
        }

    /**
     * Verifies that when an error occurs during fetching session, it should be handled properly.
     */
    @Test
    fun `GIVEN an error occurs WHEN fetchSession is called THEN it should handle the error gracefully`() {
        every { firebaseAuth.currentUser } throws RuntimeException("Error fetching user")

        assertThrows<RuntimeException> { authenticationRepository.fetchSession() }
    }

    /**
     * Verifies that when user details like displayName or email are null, it still returns a user session.
     */
    @Test
    fun `GIVEN the user has null fields WHEN fetchSession is called THEN it should return user session with null fields`() {
        val displayName = null
        val email = "fernancho@gmail.com"
        val id = "123"

        val firebaseUser: FirebaseUser = mockk()
        every { firebaseUser.uid } returns id
        every { firebaseUser.email } returns email
        every { firebaseUser.displayName } returns displayName
        every { firebaseAuth.currentUser } returns firebaseUser

        val session = authenticationRepository.fetchSession()

        session shouldBeEqualTo Session.LoggedIn(Session.User(id, email, displayName))
    }
}