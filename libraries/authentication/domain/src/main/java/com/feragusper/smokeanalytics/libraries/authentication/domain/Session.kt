package com.feragusper.smokeanalytics.libraries.authentication.domain

sealed interface Session {
    object Anonymous : Session
    data class LoggedIn(val user: User) : Session
    data class User(
        val id: String,
        val email: String?,
        val displayName: String?
    )
}
