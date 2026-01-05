package com.feragusper.smokeanalytics.libraries.authentication.domain

/**
 * Represents an authentication repository.
 */
interface AuthenticationRepository {

    /**
     * Signs out the current user.
     */
    suspend fun signOut()

    /**
     * Fetches the current session.
     *
     * @return The current session.
     */
    fun fetchSession(): Session
}