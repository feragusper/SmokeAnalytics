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

/**
 * Processes intents from the Settings feature, invoking the appropriate actions
 * and updating the result based on the outcome of these actions.
 *
 * @param fetchSessionUseCase Use case for fetching the current user session.
 * @param signOutUseCase Use case for signing out the current user.
 */
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
