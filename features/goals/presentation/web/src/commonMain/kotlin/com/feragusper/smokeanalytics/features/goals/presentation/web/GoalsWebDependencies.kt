package com.feragusper.smokeanalytics.features.goals.presentation.web

import com.feragusper.smokeanalytics.features.goals.presentation.web.process.GoalsProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase

data class GoalsWebDependencies(
    val processHolder: GoalsProcessHolder,
)

fun createGoalsWebDependencies(
    fetchSessionUseCase: FetchSessionUseCase,
    fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
    updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    fetchSmokesUseCase: FetchSmokesUseCase,
): GoalsWebDependencies = GoalsWebDependencies(
    processHolder = GoalsProcessHolder(
        fetchSessionUseCase = fetchSessionUseCase,
        fetchUserPreferencesUseCase = fetchUserPreferencesUseCase,
        updateUserPreferencesUseCase = updateUserPreferencesUseCase,
        fetchSmokesUseCase = fetchSmokesUseCase,
    )
)
