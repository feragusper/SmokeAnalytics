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
 * This class is responsible for transforming [SettingsIntent] into [SettingsResult]s,
 * encapsulating the application's business logic for managing user sessions in the Settings screen.
 *
 * @property fetchSessionUseCase Use case for fetching the current user session.
 * @property signOutUseCase Use case for signing out the current user.
 */
class SettingsProcessHolder @Inject constructor(
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val signOutUseCase: SignOutUseCase,
) : MVIProcessHolder<SettingsIntent, SettingsResult> {

    /**
     * Processes a [SettingsIntent] and transforms it into a stream of [SettingsResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A [Flow] emitting the corresponding [SettingsResult]s.
     */
    override fun processIntent(intent: SettingsIntent): Flow<SettingsResult> = when (intent) {
        SettingsIntent.FetchUser -> processFetchUser()
        SettingsIntent.SignOut -> processSignOut()
    }

    /**
     * Handles the [SettingsIntent.FetchUser] intent by fetching the current user session.
     *
     * This checks whether the user is logged in or not, and emits the appropriate state.
     *
     * @return A [Flow] emitting the result of fetching the user session.
     */
    private fun processFetchUser(): Flow<SettingsResult> = flow {
        emit(SettingsResult.Loading)
        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> emit(SettingsResult.UserLoggedOut)
            is Session.LoggedIn -> emit(SettingsResult.UserLoggedIn(session.user.email))
        }
    }

    /**
     * Handles the [SettingsIntent.SignOut] intent by signing out the current user.
     *
     * After signing out, it emits a [SettingsResult.UserLoggedOut] state.
     *
     * @return A [Flow] emitting the result of the sign-out operation.
     */
    private fun processSignOut(): Flow<SettingsResult> = flow {
        emit(SettingsResult.Loading)
        signOutUseCase()
        emit(SettingsResult.UserLoggedOut)
    }
}
