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
    val title: String,
    val targetLabel: String,
    val progressLabel: String,
    val baselineLabel: String? = null,
    val supportingText: String,
    val status: GoalStatus,
    val progressFraction: Float? = null,
    val warningLabel: String? = null,
    val celebrationLabel: String? = null,
    val streakDays: Int = 0,
    val streakLabel: String? = null,
    val isBroken: Boolean = false,
)
