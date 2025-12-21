package com.feragusper.smokeanalytics.features.authentication.presentation.process

import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Holds the logic for processing authentication-related intents and emitting corresponding results.
 *
 * @property fetchSessionUseCase Use case to fetch the current authentication session.
 * @property signOutUseCase Use case to sign out the current user.
 * @property signInWithGoogle Lambda to handle Google sign-in.
 */
class AuthenticationProcessHolder(
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val signInWithGoogle: suspend () -> Unit,
) {

    /**
     * Processes the given authentication intent and returns a flow of authentication results.
     *
     * @param intent The authentication intent to process.
     */
    fun processIntent(intent: AuthenticationIntent): Flow<AuthenticationResult> = when (intent) {
        AuthenticationIntent.FetchUser -> processFetchUser()
        AuthenticationIntent.SignInWithGoogle -> processSignIn()
        AuthenticationIntent.SignOut -> processSignOut()
        AuthenticationIntent.NavigateUp -> flow { emit(AuthenticationResult.NavigateUp) }
    }

    private fun processFetchUser(): Flow<AuthenticationResult> = flow {
        emit(AuthenticationResult.Loading)
        emit(fetchSessionResult())
    }.catch {
        emit(AuthenticationResult.Error.Generic)
    }

    private fun processSignIn(): Flow<AuthenticationResult> = flow {
        emit(AuthenticationResult.Loading)
        signInWithGoogle()
        emit(fetchSessionResult())
    }.catch {
        emit(AuthenticationResult.Error.Generic)
    }

    private fun processSignOut(): Flow<AuthenticationResult> = flow {
        emit(AuthenticationResult.Loading)
        signOutUseCase()
        emit(AuthenticationResult.UserLoggedOut)
    }.catch {
        emit(AuthenticationResult.Error.Generic)
    }

    private fun fetchSessionResult(): AuthenticationResult =
        when (fetchSessionUseCase()) {
            is Session.LoggedIn -> AuthenticationResult.UserLoggedIn
            is Session.Anonymous -> AuthenticationResult.UserLoggedOut
        }
}