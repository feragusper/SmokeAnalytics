package com.feragusper.smokeanalytics.features.goals.presentation.web.process

import com.feragusper.smokeanalytics.features.goals.domain.EvaluateGoalProgressUseCase
import com.feragusper.smokeanalytics.features.goals.domain.goalDataFetchStart
import com.feragusper.smokeanalytics.features.goals.presentation.web.mvi.GoalsIntent
import com.feragusper.smokeanalytics.features.goals.presentation.web.mvi.GoalsResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.AnalyticsTracker
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GoalsProcessHolder(
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val fetchUserPreferencesUseCase: FetchUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
    private val fetchSmokesUseCase: FetchSmokesUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val evaluateGoalProgressUseCase: EvaluateGoalProgressUseCase = EvaluateGoalProgressUseCase(),
) {

    fun processIntent(intent: GoalsIntent): Flow<GoalsResult> = when (intent) {
        GoalsIntent.FetchGoals -> processFetchGoals()
        is GoalsIntent.SaveGoal -> processUpdateGoal(intent.goal)
        GoalsIntent.ClearGoal -> processUpdateGoal(null)
    }

    private fun processFetchGoals(): Flow<GoalsResult> = flow {
        emit(GoalsResult.Loading)
        emit(fetchGoalsSnapshot())
    }.catch {
        emit(GoalsResult.ErrorGeneric("Could not load your goals."))
    }

    private fun processUpdateGoal(goal: SmokingGoal?): Flow<GoalsResult> = flow {
        emit(GoalsResult.Loading)
        val preferences = fetchUserPreferencesUseCase()
        updateUserPreferencesUseCase(preferences.copy(activeGoal = goal))
        if (goal != null) analyticsTracker.goalSet(goal.type.name) else analyticsTracker.goalCleared()
        emit(fetchGoalsSnapshot())
        emit(GoalsResult.GoalSaved)
    }.catch {
        emit(GoalsResult.ErrorGeneric("Could not save your goal."))
    }

    private suspend fun fetchGoalsSnapshot(): GoalsResult {
        val session = fetchSessionUseCase()
        if (session is Session.Anonymous) return GoalsResult.LoggedOut

        val loggedIn = session as Session.LoggedIn
        val preferences = fetchUserPreferencesUseCase()
        val smokes = fetchSmokesUseCase(start = goalDataFetchStart(preferences))
        return GoalsResult.Loaded(
            email = loggedIn.user.email,
            preferences = preferences,
            goalProgress = evaluateGoalProgressUseCase(preferences.activeGoal, smokes, preferences),
        )
    }
}
