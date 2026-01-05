package com.feragusper.smokeanalytics.features.settings.presentation.web.process

import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Represents the process holder for the Settings screen.
 *
 * @property fetchSessionUseCase The use case for fetching the session.
 * @property signOutUseCase The use case for signing out.
 */
class SettingsProcessHolder(
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val signOutUseCase: SignOutUseCase,
) {

    /**
     * Processes the given intent and returns a flow of results.
     *
     * @param intent The intent to process.
     * @return A flow of results.
     */
    fun processIntent(intent: SettingsIntent): Flow<SettingsResult> = when (intent) {
        SettingsIntent.FetchUser -> processFetchUser()
        SettingsIntent.SignOut -> processSignOut()
    }

    private fun processFetchUser(): Flow<SettingsResult> = flow {
        emit(SettingsResult.Loading)
        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> emit(SettingsResult.UserLoggedOut)
            is Session.LoggedIn -> emit(SettingsResult.UserLoggedIn(session.user.email))
        }
    }.catch {
        emit(SettingsResult.ErrorGeneric)
    }

    private fun processSignOut(): Flow<SettingsResult> = flow {
        emit(SettingsResult.Loading)
        signOutUseCase()
        emit(SettingsResult.UserLoggedOut)
    }.catch {
        emit(SettingsResult.ErrorGeneric)
    }
}