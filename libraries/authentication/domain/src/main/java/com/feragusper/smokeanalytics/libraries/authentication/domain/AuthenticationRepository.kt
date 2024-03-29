package com.feragusper.smokeanalytics.libraries.authentication.domain

/**
 * Defines the contract for authentication operations. This interface abstracts the underlying authentication
 * mechanism and provides a clear API for sign-out and session fetch operations.
 */
interface AuthenticationRepository {
    /**
     * Signs out the current user session.
     */
    fun signOut()

    /**
     * Fetches the current session state.
     *
     * @return The current [Session] state.
     */
    fun fetchSession(): Session
}
