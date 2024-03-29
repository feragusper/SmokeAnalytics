package com.feragusper.smokeanalytics.libraries.authentication.domain

import javax.inject.Inject

/**
 * Use case for signing out the current user. This use case abstracts the details of how the sign-out
 * operation is performed, allowing for easy invocation from the presentation layer.
 *
 * @property authenticationRepository The repository responsible for authentication operations.
 */
class SignOutUseCase @Inject constructor(private val authenticationRepository: AuthenticationRepository) {

    /**
     * Invokes the use case to perform the sign-out operation.
     */
    operator fun invoke() = authenticationRepository.signOut()
}
