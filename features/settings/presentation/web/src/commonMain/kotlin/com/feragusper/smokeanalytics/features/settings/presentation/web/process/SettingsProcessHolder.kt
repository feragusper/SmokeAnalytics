package com.feragusper.smokeanalytics.features.settings.presentation.web.process

import com.feragusper.smokeanalytics.features.goals.domain.EvaluateGoalProgressUseCase
import com.feragusper.smokeanalytics.features.goals.domain.goalDataFetchStart
import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.web.mvi.SettingsResult
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
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
    private val fetchSmokesUseCase: FetchSmokesUseCase,
    private val evaluateGoalProgressUseCase: EvaluateGoalProgressUseCase = EvaluateGoalProgressUseCase(),
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
            is Session.LoggedIn -> {
                val preferences = fetchUserPreferencesUseCase()
                val smokes = runCatching { fetchSmokesUseCase(start = goalDataFetchStart(preferences)) }.getOrDefault(emptyList())
                emit(
                    SettingsResult.UserLoggedIn(
                        email = session.user.email,
                        displayName = session.user.displayName,
                        preferences = preferences,
                        goalProgress = evaluateGoalProgressUseCase(preferences.activeGoal, smokes, preferences),
                    )
                )
            }
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
        val preferences = intent.preferences
        updateUserPreferencesUseCase(preferences)
        val savedPreferences = fetchUserPreferencesUseCase()
        val smokes = runCatching { fetchSmokesUseCase(start = goalDataFetchStart(savedPreferences)) }.getOrDefault(emptyList())
        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> emit(SettingsResult.UserLoggedOut)
            is Session.LoggedIn -> emit(
                SettingsResult.UserLoggedIn(
                    email = session.user.email,
                    displayName = session.user.displayName,
                    preferences = savedPreferences,
                    goalProgress = evaluateGoalProgressUseCase(savedPreferences.activeGoal, smokes, savedPreferences),
                )
            )
        }
        emit(SettingsResult.PreferencesSaved)
    }.catch {
        emit(SettingsResult.ErrorGeneric())
    }
}
