package com.feragusper.smokeanalytics.libraries.authentication.domain

interface AuthenticationRepository {
    suspend fun signOut()
    fun fetchSession(): Session
}