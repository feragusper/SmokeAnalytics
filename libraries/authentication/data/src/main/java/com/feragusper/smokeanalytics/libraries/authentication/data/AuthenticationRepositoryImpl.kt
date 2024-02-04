package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) :
    AuthenticationRepository {

    override fun signOut() = firebaseAuth.signOut()

    override fun fetchSession() = firebaseAuth.toSession()
}

private fun FirebaseAuth.toSession() = when (currentUser) {
    null -> Session.Anonymous
    else -> Session.LoggedIn(currentUser?.toUser() ?: throw IllegalStateException("User is null"))
}

private fun FirebaseUser.toUser(): Session.User {
    return Session.User(
        id = this.uid,
        email = this.email,
        displayName = this.displayName,
    )
}
