package com.feragusper.smokeanalytics.features.settings.presentation.web

import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.SignOutUseCase

class SettingsWebDependencies(
    val processHolder: SettingsProcessHolder,
)

fun createSettingsWebDependencies(
    fetchSessionUseCase: FetchSessionUseCase,
    signOutUseCase: SignOutUseCase,
): SettingsWebDependencies {
    return SettingsWebDependencies(
        processHolder = SettingsProcessHolder(
            fetchSessionUseCase = fetchSessionUseCase,
            signOutUseCase = signOutUseCase,
        )
    )
}