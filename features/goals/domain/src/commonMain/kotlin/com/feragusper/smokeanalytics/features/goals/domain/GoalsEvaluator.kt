package com.feragusper.smokeanalytics.features.goals.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.activeCurrentDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentMonthStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentWeekStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextMonthStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextWeekStartInstant
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlin.math.roundToInt
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.until

class EvaluateGoalProgressUseCase(
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {

    operator fun invoke(
        activeGoal: SmokingGoal?,
        smokes: List<Smoke>,
        preferences: UserPreferences,
        now: Instant = Clock.System.now(),
    ): GoalProgress? {
        if (activeGoal == null) return null

        return when (activeGoal) {
            is SmokingGoal.DailyCap -> evaluateDailyCap(activeGoal, smokes, preferences, now)
            is SmokingGoal.ReductionVsPreviousWeek -> evaluateReductionVsPreviousWeek(activeGoal, smokes, preferences, now)
            is SmokingGoal.ReductionVsPreviousMonth -> evaluateReductionVsPreviousMonth(activeGoal, smokes, preferences, now)
            is SmokingGoal.MindfulGap -> evaluateMindfulGap(activeGoal, smokes, preferences, now)
        }
    }

    private fun evaluateDailyCap(
        goal: SmokingGoal.DailyCap,
        smokes: List<Smoke>,
        preferences: UserPreferences,
        now: Instant,
    ): GoalProgress {
        val currentDayStart = activeCurrentDayStartInstant(
            now = now,
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val nextDayStart = nextDayStartInstant(
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val todayCount = smokes.count { it.date >= currentDayStart && it.date < nextDayStart }
        val remaining = (goal.maxCigarettesPerDay - todayCount).coerceAtLeast(0)
        val status = when {
            todayCount < goal.maxCigarettesPerDay -> GoalStatus.OnTrack
            todayCount == goal.maxCigarettesPerDay -> GoalStatus.Completed
            else -> GoalStatus.OffTrack
        }

        return GoalProgress(
            goal = goal,
            title = "Daily cap",
            targetLabel = "Target: at most ${goal.maxCigarettesPerDay} today",
            progressLabel = "$todayCount smoked today",
            supportingText = when (status) {
                GoalStatus.OnTrack -> "$remaining left before reaching today's cap."
                GoalStatus.Completed -> "You reached today's cap. Holding here keeps the goal intact."
                GoalStatus.OffTrack -> "Today's cap is exceeded. The next win is stopping the count from climbing further."
                GoalStatus.NotEnoughData -> ""
            },
            status = status,
        )
    }

    private fun evaluateReductionVsPreviousWeek(
        goal: SmokingGoal.ReductionVsPreviousWeek,
        smokes: List<Smoke>,
        preferences: UserPreferences,
        now: Instant,
    ): GoalProgress {
        val currentStart = currentWeekStartInstant(
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val currentEnd = nextWeekStartInstant(
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val previousStart = currentStart.minus(7, DateTimeUnit.DAY, timeZone)
        val previousEnd = currentStart
        return evaluateReduction(
            title = "Reduction vs previous week",
            goal = goal,
            currentCount = smokes.count { it.date >= currentStart && it.date < currentEnd },
            baselineCount = smokes.count { it.date >= previousStart && it.date < previousEnd },
            baselineLabel = "Compared with the previous week",
        )
    }

    private fun evaluateReductionVsPreviousMonth(
        goal: SmokingGoal.ReductionVsPreviousMonth,
        smokes: List<Smoke>,
        preferences: UserPreferences,
        now: Instant,
    ): GoalProgress {
        val currentStart = currentMonthStartInstant(
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val currentEnd = nextMonthStartInstant(
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val previousStart = currentStart.minus(1, DateTimeUnit.MONTH, timeZone)
        val previousEnd = currentStart
        return evaluateReduction(
            title = "Reduction vs previous month",
            goal = goal,
            currentCount = smokes.count { it.date >= currentStart && it.date < currentEnd },
            baselineCount = smokes.count { it.date >= previousStart && it.date < previousEnd },
            baselineLabel = "Compared with the previous month",
        )
    }

    private fun evaluateReduction(
        title: String,
        goal: SmokingGoal,
        currentCount: Int,
        baselineCount: Int,
        baselineLabel: String,
    ): GoalProgress {
        val reductionPercent = when (goal) {
            is SmokingGoal.ReductionVsPreviousWeek -> goal.reductionPercent
            is SmokingGoal.ReductionVsPreviousMonth -> goal.reductionPercent
            else -> 0.0
        }
        if (baselineCount <= 0) {
            return GoalProgress(
                goal = goal,
                title = title,
                targetLabel = "Target: reduce by ${reductionPercent.toWholeOrOneDecimal()}%",
                progressLabel = "Waiting for a baseline",
                baselineLabel = baselineLabel,
                supportingText = "A previous comparison period is needed before this goal can be evaluated reliably.",
                status = GoalStatus.NotEnoughData,
            )
        }

        val targetCount = (baselineCount * (1.0 - reductionPercent / 100.0)).coerceAtLeast(0.0)
        val currentReduction = ((baselineCount - currentCount).toDouble() / baselineCount.toDouble()) * 100.0
        val status = when {
            currentCount <= targetCount -> GoalStatus.Completed
            currentReduction >= reductionPercent -> GoalStatus.OnTrack
            else -> GoalStatus.OffTrack
        }

        return GoalProgress(
            goal = goal,
            title = title,
            targetLabel = "Target: ${targetCount.toWholeOrOneDecimal()} smokes or fewer",
            progressLabel = "Current period: $currentCount vs $baselineCount baseline",
            baselineLabel = baselineLabel,
            supportingText = when (status) {
                GoalStatus.Completed -> "You are already below the target pace for this comparison window."
                GoalStatus.OnTrack -> "The current pace is below the previous period and moving in the right direction."
                GoalStatus.OffTrack -> "The current pace is still above the planned reduction. A few fewer entries will move it back on track."
                GoalStatus.NotEnoughData -> ""
            },
            status = status,
        )
    }

    private fun evaluateMindfulGap(
        goal: SmokingGoal.MindfulGap,
        smokes: List<Smoke>,
        preferences: UserPreferences,
        now: Instant,
    ): GoalProgress {
        val lastSmoke = smokes.maxByOrNull { it.date }
        val elapsedMinutes = if (lastSmoke == null) {
            preferences.awakeMinutesPerDay
        } else {
            lastSmoke.date.until(now, DateTimeUnit.MINUTE, timeZone).toInt().coerceAtLeast(0)
        }
        val status = when {
            elapsedMinutes > goal.targetMinutes -> GoalStatus.Completed
            elapsedMinutes == goal.targetMinutes -> GoalStatus.Completed
            elapsedMinutes >= (goal.targetMinutes * 0.7f).roundToInt() -> GoalStatus.OnTrack
            else -> GoalStatus.OffTrack
        }

        return GoalProgress(
            goal = goal,
            title = "Mindful gap",
            targetLabel = "Target: wait ${goal.targetMinutes.formatMinutesLabel()} between cigarettes",
            progressLabel = "Current gap: ${elapsedMinutes.formatMinutesLabel()}",
            supportingText = when (status) {
                GoalStatus.Completed -> "The latest gap already meets the target."
                GoalStatus.OnTrack -> "The current gap is building toward the target interval."
                GoalStatus.OffTrack -> "The gap is still short of the target. The next few minutes matter most."
                GoalStatus.NotEnoughData -> ""
            },
            status = status,
        )
    }
}

fun goalDataFetchStart(
    preferences: UserPreferences,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant = currentMonthStartInstant(
    timeZone = timeZone,
    dayStartHour = preferences.dayStartHour,
    manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
).minus(1, DateTimeUnit.MONTH, timeZone)

private fun Double.toWholeOrOneDecimal(): String {
    val rounded = (this * 10.0).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

private fun Int.formatMinutesLabel(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours <= 0 -> "${minutes}m"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}
