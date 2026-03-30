package com.feragusper.smokeanalytics.libraries.preferences.domain

enum class GoalType {
    DailyCap,
    ReductionVsPreviousWeek,
    ReductionVsPreviousMonth,
    MindfulGap,
}

sealed class SmokingGoal {
    abstract val type: GoalType
    abstract val metricValue: Double

    data class DailyCap(
        val maxCigarettesPerDay: Int,
    ) : SmokingGoal() {
        override val type: GoalType = GoalType.DailyCap
        override val metricValue: Double = maxCigarettesPerDay.toDouble()
    }

    data class ReductionVsPreviousWeek(
        val reductionPercent: Double,
    ) : SmokingGoal() {
        override val type: GoalType = GoalType.ReductionVsPreviousWeek
        override val metricValue: Double = reductionPercent
    }

    data class ReductionVsPreviousMonth(
        val reductionPercent: Double,
    ) : SmokingGoal() {
        override val type: GoalType = GoalType.ReductionVsPreviousMonth
        override val metricValue: Double = reductionPercent
    }

    data class MindfulGap(
        val targetMinutes: Int,
    ) : SmokingGoal() {
        override val type: GoalType = GoalType.MindfulGap
        override val metricValue: Double = targetMinutes.toDouble()
    }
}

fun smokingGoalOrNull(
    type: GoalType?,
    metricValue: Double?,
): SmokingGoal? = when {
    type == null || metricValue == null -> null
    type == GoalType.DailyCap -> SmokingGoal.DailyCap(metricValue.toInt().coerceAtLeast(1))
    type == GoalType.ReductionVsPreviousWeek -> SmokingGoal.ReductionVsPreviousWeek(metricValue)
    type == GoalType.ReductionVsPreviousMonth -> SmokingGoal.ReductionVsPreviousMonth(metricValue)
    type == GoalType.MindfulGap -> SmokingGoal.MindfulGap(metricValue.toInt().coerceAtLeast(1))
    else -> null
}
