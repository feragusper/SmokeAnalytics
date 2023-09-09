package com.feragusper.smokeanalytics.libraries.authentication.domain

interface AuthenticationRepository {
    fun signOut()
    fun fetchSession() : Session
}