package com.feragusper.smokeanalytics.libraries.authentication.data

import com.feragusper.smokeanalytics.libraries.authentication.domain.AuthenticationRepository
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth

class AuthenticationRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = Firebase.auth,
) : AuthenticationRepository {

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

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