package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepositoryImpl @Inject constructor() : AuthenticationRepository {

    override fun signOut() = Firebase.auth.signOut()

    override fun fetchSession() = Firebase.auth.toSession()
}

private fun FirebaseAuth.toSession() = when (currentUser) {
    null -> Session.Anonymous
    else -> Session.Logged(currentUser?.toUser() ?: throw IllegalStateException("User is null"))
}

private fun FirebaseUser.toUser(): Session.User {
    return Session.User(
        displayName = this.displayName
    )
}
