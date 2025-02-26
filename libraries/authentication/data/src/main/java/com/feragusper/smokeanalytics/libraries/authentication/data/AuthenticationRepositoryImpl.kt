package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An implementation of [AuthenticationRepository] that uses Firebase Authentication for managing user sessions.
 *
 * @property firebaseAuth The FirebaseAuth instance used for authentication operations.
 */
@Singleton
class AuthenticationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : AuthenticationRepository {

    /**
     * Signs out the current user from Firebase Authentication.
     */
    override fun signOut() = firebaseAuth.signOut()

    /**
     * Fetches the current session state from Firebase Authentication, converting it into a [Session] domain model.
     *
     * @return The current user session as a [Session] instance.
     */
    override fun fetchSession() = firebaseAuth.toSession()
}

/**
 * Converts a FirebaseAuth instance to a [Session] domain model.
 *
 * @return The session state as a [Session].
 */
private fun FirebaseAuth.toSession(): Session = when (currentUser) {
    null -> Session.Anonymous
    else -> Session.LoggedIn(currentUser?.toUser() ?: throw IllegalStateException("User is null"))
}

/**
 * Converts a FirebaseUser to a [Session.User] domain model.
 *
 * @return The FirebaseUser as a [Session.User].
 */
private fun FirebaseUser.toUser(): Session.User = Session.User(
    id = this.uid,
    email = this.email,
    displayName = this.displayName
)
