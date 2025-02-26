package com.feragusper.smokeanalytics.libraries.authentication.domain

import javax.inject.Inject

/**
 * Use case for fetching the current session state. This use case abstracts the details of how the session
 * state is retrieved, making it easy to call from the presentation layer.
 *
 * @property authenticationRepository The repository responsible for authentication operations.
 */
class FetchSessionUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) {

    /**
     * Invokes the use case to retrieve the current session state.
     *
     * @return The current [Session] state.
     */
    operator fun invoke() = authenticationRepository.fetchSession()
}
