package com.feragusper.smokeanalytics.features.settings.presentation.web.process

import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
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
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
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
        is SettingsIntent.UpdatePreferences -> processUpdatePreferences(intent)
    }

    private fun processFetchUser(): Flow<SettingsResult> = flow {
        emit(SettingsResult.Loading)
        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> emit(SettingsResult.UserLoggedOut)
            is Session.LoggedIn -> emit(
                SettingsResult.UserLoggedIn(
                    email = session.user.email,
                    displayName = session.user.displayName,
                    preferences = runCatching { fetchUserPreferencesUseCase() }.getOrDefault(UserPreferences()),
                )
            )
        }
    }.catch {
        emit(SettingsResult.ErrorGeneric())
    }

    private fun processSignOut(): Flow<SettingsResult> = flow {
        emit(SettingsResult.Loading)
        signOutUseCase()
        emit(SettingsResult.UserLoggedOut)
    }.catch {
        emit(SettingsResult.ErrorGeneric())
    }

    private fun processUpdatePreferences(intent: SettingsIntent.UpdatePreferences): Flow<SettingsResult> = flow {
        emit(SettingsResult.Loading)
        val preferences = UserPreferences(
            packPrice = intent.packPrice,
            cigarettesPerPack = intent.cigarettesPerPack,
            dayStartHour = intent.dayStartHour,
            bedtimeHour = intent.bedtimeHour,
            locationTrackingEnabled = intent.locationTrackingEnabled,
            currencySymbol = intent.currencySymbol,
        )
        updateUserPreferencesUseCase(preferences)
        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> emit(SettingsResult.UserLoggedOut)
            is Session.LoggedIn -> emit(
                SettingsResult.UserLoggedIn(
                    email = session.user.email,
                    displayName = session.user.displayName,
                    preferences = preferences,
                )
            )
        }
        emit(SettingsResult.PreferencesSaved)
    }.catch {
        emit(SettingsResult.ErrorGeneric())
    }
}
