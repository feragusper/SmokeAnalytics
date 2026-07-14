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
) {
    /** True when a completed-day streak exists (drives the streak line in presentation). */
    val hasStreak: Boolean get() = streakDays > 0
}
