package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.features.goals.domain.EvaluateGoalProgressUseCase
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Swift-friendly view of the active goal's progress. [progressFraction] is -1 when unknown;
 * [hasGoal] is false when no goal is set (the user sets one on Android/web for now).
 */
data class GoalSnapshot(
    val hasGoal: Boolean,
    val title: String,
    val detail: String,
    val statusLabel: String,
    val progressFraction: Double,
    val streakDays: Int,
)

/** Read-only Goals entry point: reads the active goal + preferences and evaluates progress. */
class GoalsFacade : KoinComponent {

    private val fetchPreferences: FetchUserPreferencesUseCase by inject()
    private val fetchSmokes: FetchSmokesUseCase by inject()
    private val evaluate: EvaluateGoalProgressUseCase by inject()

    @Throws(Throwable::class)
    suspend fun load(): GoalSnapshot {
        val preferences = fetchPreferences()
        val goal = preferences.activeGoal
            ?: return GoalSnapshot(false, "", "", "", -1.0, 0)

        // Reduction goals compare against the previous week/month, so pull a wider window.
        val smokes = fetchSmokes(Clock.System.now().minus(60.days), null)
        val progress = evaluate(goal, smokes, preferences)
            ?: return GoalSnapshot(true, goal.title(), goal.detail(), "Not enough data yet", -1.0, 0)

        return GoalSnapshot(
            hasGoal = true,
            title = goal.title(),
            detail = goal.detail(),
            statusLabel = progress.status.label(),
            progressFraction = progress.progressFraction?.toDouble() ?: -1.0,
            streakDays = progress.streakDays,
        )
    }

    private fun SmokingGoal.title(): String = when (this) {
        is SmokingGoal.DailyCap -> "Daily cap"
        is SmokingGoal.ReductionVsPreviousWeek -> "Weekly reduction"
        is SmokingGoal.ReductionVsPreviousMonth -> "Monthly reduction"
        is SmokingGoal.MindfulGap -> "Mindful gap"
    }

    private fun SmokingGoal.detail(): String = when (this) {
        is SmokingGoal.DailyCap -> "Max $maxCigarettesPerDay cigarettes per day"
        is SmokingGoal.ReductionVsPreviousWeek -> "${reductionPercent.toInt()}% fewer than last week"
        is SmokingGoal.ReductionVsPreviousMonth -> "${reductionPercent.toInt()}% fewer than last month"
        is SmokingGoal.MindfulGap -> "At least $targetMinutes min between cigarettes"
    }

    private fun GoalStatus.label(): String = when (this) {
        GoalStatus.OnTrack -> "On track"
        GoalStatus.OffTrack -> "Off track"
        GoalStatus.Completed -> "Completed"
        GoalStatus.NotEnoughData -> "Not enough data yet"
    }
}
