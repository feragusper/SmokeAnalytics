package com.feragusper.smokeanalytics.libraries.authentication.domain

/**
 * Represents the state of a user session. This sealed interface is used to differentiate between logged-in
 * and anonymous sessions, providing a type-safe way to handle session states.
 */
sealed interface Session {
    /**
     * Represents an anonymous session, where no user is currently signed in.
     */
    object Anonymous : Session

    /**
     * Represents a logged-in session, including information about the user.
     *
     * @param user The [User] information for the logged-in session.
     */
    data class LoggedIn(val user: User) : Session

    /**
     * Represents a user with an ID, email, and display name. This data class encapsulates the
     * user information available in a logged-in session.
     *
     * @param id The unique identifier for the user.
     * @param email The user's email address, if available.
     * @param displayName The user's display name, if available.
     */
    data class User(
        val id: String,
        val email: String?,
        val displayName: String?
    )
}
