package com.feragusper.smokeanalytics.features.authentication.presentation.process

import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Process holder for managing authentication-related intents and transforming them into results.
 *
 * This class handles business logic related to user authentication, including fetching user session details.
 *
 * @property fetchSessionUseCase Use case for fetching the current user session state.
 */
class AuthenticationProcessHolder @Inject constructor(
    private val fetchSessionUseCase: FetchSessionUseCase,
) : MVIProcessHolder<AuthenticationIntent, AuthenticationResult> {

    /**
     * Processes an [AuthenticationIntent] and transforms it into a stream of [AuthenticationResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A [Flow] emitting the corresponding [AuthenticationResult]s.
     */
    override fun processIntent(intent: AuthenticationIntent): Flow<AuthenticationResult> =
        when (intent) {
            is AuthenticationIntent.FetchUser -> processFetchUser()
            AuthenticationIntent.NavigateUp -> flow { emit(AuthenticationResult.NavigateUp) }
        }

    /**
     * Processes the [AuthenticationIntent.FetchUser] intent by fetching the user session state
     * and emitting the corresponding [AuthenticationResult].
     *
     * @return A [Flow] emitting the result of the user session fetch operation.
     */
    private fun processFetchUser(): Flow<AuthenticationResult> = flow {
        emit(AuthenticationResult.Loading)
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> emit(AuthenticationResult.Error.Generic)
            is Session.LoggedIn -> emit(AuthenticationResult.UserLoggedIn)
        }
    }
}
