package com.feragusper.smokeanalytics.features.goals.domain

import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal

enum class GoalStatus {
    OnTrack,
    OffTrack,
    Completed,
    NotEnoughData,
}

data class GoalProgress(
    val goal: SmokingGoal,
    val titleKind: GoalTitleKind = GoalTitleKind.DailyCap,
    val target: GoalTargetSpec = GoalTargetSpec.DailyCap(0),
    val progress: GoalProgressSpec = GoalProgressSpec.WaitingBaseline,
    val baseline: GoalBaselineKind? = null,
    val supporting: GoalSupportingSpec = GoalSupportingSpec.None,
    val status: GoalStatus,
    val progressFraction: Float? = null,
    val warning: GoalWarningKind? = null,
    val celebration: GoalCelebrationKind? = null,
    val streakDays: Int = 0,
    val isBroken: Boolean = false,
    val weeklyScore: GoalScore? = null,
    val monthlyScore: GoalScore? = null,
) {
    /** True when a completed-day streak exists (drives the streak line in presentation). */
    val hasStreak: Boolean get() = streakDays > 0
}

/**
 * Scoreboard for a window (week or month) of a daily-cap goal.
 *
 * [completedDays] counts every tracked day in the window that stayed within the cap, even when
 * those days are not consecutive; [longestStreak] is the longest run of consecutive completed days
 * inside the window. [points] rewards both: each completed day scores 1 point and every day that
 * continues a streak scores 1 more (a run of L days is worth 2L - 1 points).
 */
data class GoalScore(
    val completedDays: Int,
    val trackedDays: Int,
    val longestStreak: Int,
    val points: Int,
)
