package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth

/**
 * Represents an authentication repository.
 *
 * @property firebaseAuth The Firebase authentication.
 */
class AuthenticationRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = Firebase.auth,
) : AuthenticationRepository {

    /**
     * @see AuthenticationRepository.signOut
     */
    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    /**
     * @see AuthenticationRepository.fetchSession
     */
    override fun fetchSession(): Session = firebaseAuth.toSession()
}

private fun FirebaseAuth.toSession(): Session = when (currentUser) {
    null -> Session.Anonymous
    else -> Session.LoggedIn(currentUser?.toUser() ?: error("User is null"))
}

private fun FirebaseUser.toUser(): Session.User = Session.User(
    id = uid,
    email = email,
    displayName = displayName,
)