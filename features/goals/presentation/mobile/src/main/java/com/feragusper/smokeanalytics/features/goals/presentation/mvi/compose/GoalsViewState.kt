package com.feragusper.smokeanalytics.features.goals.presentation.mvi.compose

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.goals.presentation.GoalsEditorScreen
import com.feragusper.smokeanalytics.features.goals.presentation.mvi.GoalsIntent
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences

data class GoalsViewState(
    internal val displayLoading: Boolean = false,
    internal val currentEmail: String? = null,
    internal val preferences: UserPreferences = UserPreferences(),
    internal val goalProgress: GoalProgress? = null,
    internal val infoMessage: String? = null,
    internal val errorMessage: String? = null,
    internal val signInErrorMessage: String? = null,
) : MVIViewState<GoalsIntent> {

    @Composable
    fun Compose(
        intent: (GoalsIntent) -> Unit,
        navigateBack: () -> Unit,
    ) {
        GoalsEditorScreen(
            currentEmail = currentEmail,
            preferences = preferences,
            goalProgress = goalProgress,
            displayLoading = displayLoading,
            errorMessage = errorMessage,
            signInErrorMessage = signInErrorMessage,
            onBack = navigateBack,
            onSaveGoal = { goal -> intent(GoalsIntent.SaveGoal(goal)) },
            onClearGoal = { intent(GoalsIntent.ClearGoal) },
            onSignInSuccess = { intent(GoalsIntent.FetchGoals) },
            onSignInError = { intent(GoalsIntent.FetchGoals) },
        )
    }
}
