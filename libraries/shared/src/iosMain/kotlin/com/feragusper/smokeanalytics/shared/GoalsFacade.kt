package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.features.goals.domain.EvaluateGoalProgressUseCase
import com.feragusper.smokeanalytics.features.goals.domain.GoalCelebrationKind
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.features.goals.domain.GoalWarningKind
import com.feragusper.smokeanalytics.libraries.preferences.domain.FetchUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UpdateUserPreferencesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Swift-friendly view of the active goal's progress. [progressFraction] is -1 when unknown;
 * [hasGoal] is false when no goal is set. [goalTypeKey]/[goalValue] prefill the editor
 * (empty / 0 when there is no goal).
 */
data class GoalSnapshot(
    val hasGoal: Boolean,
    val title: String,
    val detail: String,
    val statusLabel: String,
    val progressFraction: Double,
    val streakDays: Int,
    val goalTypeKey: String,
    val goalValue: Int,
    val warning: String,
    val celebration: String,
    val weeklyPoints: Int,
    val weeklyCompletedDays: Int,
    val weeklyTrackedDays: Int,
)

/** Goal type keys shared with the Swift editor. */
object GoalTypeKeys {
    const val DAILY_CAP = "daily_cap"
    const val REDUCTION_WEEK = "reduction_week"
    const val REDUCTION_MONTH = "reduction_month"
    const val MINDFUL_GAP = "mindful_gap"
}

/** Goals entry point: reads/evaluates the active goal and creates/clears it. */
class GoalsFacade : KoinComponent {

    private val fetchPreferences: FetchUserPreferencesUseCase by inject()
    private val updatePreferences: UpdateUserPreferencesUseCase by inject()
    private val fetchSmokes: FetchSmokesUseCase by inject()
    private val evaluate: EvaluateGoalProgressUseCase by inject()

    @Throws(Throwable::class)
    suspend fun load(): GoalSnapshot {
        val preferences = fetchPreferences()
        val goal = preferences.activeGoal
            ?: return GoalSnapshot(false, "", "", "", -1.0, 0, "", 0, "", "", 0, 0, 0)

        // Reduction goals compare against the previous week/month, so pull a wider window.
        val smokes = fetchSmokes(Clock.System.now().minus(60.days), null)
        val progress = evaluate(goal, smokes, preferences)
        val week = progress?.weeklyScore
        return GoalSnapshot(
            hasGoal = true,
            title = goal.title(),
            detail = goal.detail(),
            statusLabel = progress?.status?.label() ?: "Not enough data yet",
            progressFraction = progress?.progressFraction?.toDouble() ?: -1.0,
            streakDays = progress?.streakDays ?: 0,
            goalTypeKey = goal.typeKey(),
            goalValue = goal.editorValue(),
            warning = progress?.warning?.label() ?: "",
            celebration = progress?.celebration?.label() ?: "",
            weeklyPoints = week?.points ?: 0,
            weeklyCompletedDays = week?.completedDays ?: 0,
            weeklyTrackedDays = week?.trackedDays ?: 0,
        )
    }

    private fun GoalWarningKind.label(): String = when (this) {
        GoalWarningKind.OneMoreBreaksCap -> "One more breaks your cap"
        GoalWarningKind.CapBroken -> "You went over your cap today"
    }

    private fun GoalCelebrationKind.label(): String = when (this) {
        GoalCelebrationKind.ReachedCapHold -> "You've hit your cap — hold here"
        GoalCelebrationKind.YesterdayUnderCap -> "You stayed under yesterday 🎉"
    }

    /** Creates/replaces the active goal from the editor's type key + value. */
    @Throws(Throwable::class)
    suspend fun saveGoal(typeKey: String, value: Int) {
        val goal = when (typeKey) {
            GoalTypeKeys.DAILY_CAP -> SmokingGoal.DailyCap(value)
            GoalTypeKeys.REDUCTION_WEEK -> SmokingGoal.ReductionVsPreviousWeek(value.toDouble())
            GoalTypeKeys.REDUCTION_MONTH -> SmokingGoal.ReductionVsPreviousMonth(value.toDouble())
            GoalTypeKeys.MINDFUL_GAP -> SmokingGoal.MindfulGap(value)
            else -> return
        }
        updatePreferences(fetchPreferences().copy(activeGoal = goal))
    }

    /** Removes the active goal. */
    @Throws(Throwable::class)
    suspend fun clearGoal() {
        updatePreferences(fetchPreferences().copy(activeGoal = null))
    }

    private fun SmokingGoal.typeKey(): String = when (this) {
        is SmokingGoal.DailyCap -> GoalTypeKeys.DAILY_CAP
        is SmokingGoal.ReductionVsPreviousWeek -> GoalTypeKeys.REDUCTION_WEEK
        is SmokingGoal.ReductionVsPreviousMonth -> GoalTypeKeys.REDUCTION_MONTH
        is SmokingGoal.MindfulGap -> GoalTypeKeys.MINDFUL_GAP
    }

    private fun SmokingGoal.editorValue(): Int = when (this) {
        is SmokingGoal.DailyCap -> maxCigarettesPerDay
        is SmokingGoal.ReductionVsPreviousWeek -> reductionPercent.toInt()
        is SmokingGoal.ReductionVsPreviousMonth -> reductionPercent.toInt()
        is SmokingGoal.MindfulGap -> targetMinutes
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
