package com.feragusper.smokeanalytics.features.settings.presentation.process

import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SettingsProcessHolder @Inject constructor(
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val signOutUseCase: SignOutUseCase,
) : MVIProcessHolder<SettingsIntent, SettingsResult> {

    override fun processIntent(intent: SettingsIntent): Flow<SettingsResult> = when (intent) {
        SettingsIntent.FetchUser -> processFetchUser()
        SettingsIntent.SignOut -> processSignOut()
    }

    private fun processFetchUser(): Flow<SettingsResult> = flow {
        emit(SettingsResult.Loading)
        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> emit(SettingsResult.UserLoggedOut)
            is Session.LoggedIn -> emit(SettingsResult.UserLoggedIn(session.user.email))
        }
    }

    private fun processSignOut(): Flow<SettingsResult> = flow {
        emit(SettingsResult.Loading)
        signOutUseCase()
        emit(SettingsResult.UserLoggedOut)
    }
}
