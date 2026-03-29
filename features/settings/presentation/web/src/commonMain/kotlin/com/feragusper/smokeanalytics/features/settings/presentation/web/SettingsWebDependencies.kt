package com.feragusper.smokeanalytics.features.settings.presentation.web

import com.feragusper.smokeanalytics.features.settings.presentation.web.process.SettingsProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase

/**
 * Represents the dependencies for the Settings screen.
 *
 * @property processHolder The process holder for the Settings screen.
 */
class SettingsWebDependencies(
    val processHolder: SettingsProcessHolder,
)

/**
 * Creates the dependencies for the Settings screen.
 *
 * @param fetchSessionUseCase The use case for fetching the session.
 * @param signOutUseCase The use case for signing out.
 *
 * @return The dependencies for the Settings screen.
 */
fun createSettingsWebDependencies(
    fetchSessionUseCase: FetchSessionUseCase,
    signOutUseCase: SignOutUseCase,
    fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
    updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    fetchSmokesUseCase: FetchSmokesUseCase,
): SettingsWebDependencies {
    return SettingsWebDependencies(
        processHolder = SettingsProcessHolder(
            fetchSessionUseCase = fetchSessionUseCase,
            signOutUseCase = signOutUseCase,
            fetchUserPreferencesUseCase = fetchUserPreferencesUseCase,
            updateUserPreferencesUseCase = updateUserPreferencesUseCase,
            fetchSmokesUseCase = fetchSmokesUseCase,
        )
    )
}
