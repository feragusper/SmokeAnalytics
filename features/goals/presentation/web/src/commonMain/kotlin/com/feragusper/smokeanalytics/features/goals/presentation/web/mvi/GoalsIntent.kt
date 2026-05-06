package com.feragusper.smokeanalytics.features.goals.presentation.web.mvi

import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal

sealed interface GoalsIntent {
    data object FetchGoals : GoalsIntent
    data class SaveGoal(val goal: SmokingGoal) : GoalsIntent
    data object ClearGoal : GoalsIntent
}
