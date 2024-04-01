package com.feragusper.smokeanalytics.features.authentication.presentation.process

import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationIntent
import com.feragusper.smokeanalytics.features.authentication.presentation.mvi.AuthenticationResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthenticationProcessHolder @Inject constructor(
    private val fetchSessionUseCase: FetchSessionUseCase,
) : MVIProcessHolder<AuthenticationIntent, AuthenticationResult> {

    override fun processIntent(intent: AuthenticationIntent): Flow<AuthenticationResult> =
        when (intent) {
            is AuthenticationIntent.FetchUser -> processFetchUser()
            AuthenticationIntent.NavigateUp -> flow { emit(AuthenticationResult.NavigateUp) }
        }

    private fun processFetchUser(): Flow<AuthenticationResult> = flow {
        emit(AuthenticationResult.Loading)
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> emit(AuthenticationResult.Error.Generic)
            is Session.LoggedIn -> emit(AuthenticationResult.UserLoggedIn)
        }
    }

}
