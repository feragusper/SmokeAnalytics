package com.feragusper.smokeanalytics.features.goals.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal

sealed class GoalsIntent : MVIIntent {
    data object FetchGoals : GoalsIntent()
    data class SaveGoal(val goal: SmokingGoal) : GoalsIntent()
    data object ClearGoal : GoalsIntent()
}
