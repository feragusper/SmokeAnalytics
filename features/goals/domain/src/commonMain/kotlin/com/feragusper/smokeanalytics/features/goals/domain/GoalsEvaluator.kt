package com.feragusper.smokeanalytics.features.goals.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.activeCurrentDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentBucketDate
import com.feragusper.smokeanalytics.libraries.architecture.domain.dayBucketDate
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentMonthStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentWeekStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextMonthStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextWeekStartInstant
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlin.math.roundToInt
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.until
import kotlin.time.Clock

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
        val currentBucketDate = currentBucketDate(
            now = now,
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val countsByBucketDate = smokes.groupingBy {
            it.date.dayBucketDate(
                timeZone = timeZone,
                dayStartHour = preferences.dayStartHour,
                manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
            )
        }.eachCount()
        val yesterdayBucketDate = currentBucketDate.minus(1, DateTimeUnit.DAY)
        val yesterdayCount = countsByBucketDate[yesterdayBucketDate] ?: 0
        val hasHistoryBeforeToday = countsByBucketDate.keys.any { it < currentBucketDate }
        val yesterdayCompleted = hasHistoryBeforeToday && yesterdayCount <= goal.maxCigarettesPerDay
        val streakDays = countsByBucketDate.consecutiveCompletedDays(
            endDateInclusive = yesterdayBucketDate,
            maxPerDay = goal.maxCigarettesPerDay,
        )
        val status = when {
            todayCount < goal.maxCigarettesPerDay -> GoalStatus.OnTrack
            todayCount == goal.maxCigarettesPerDay -> GoalStatus.Completed
            else -> GoalStatus.OffTrack
        }
        val warning = when {
            todayCount == goal.maxCigarettesPerDay - 1 -> GoalWarningKind.OneMoreBreaksCap
            todayCount > goal.maxCigarettesPerDay -> GoalWarningKind.CapBroken
            else -> null
        }
        val celebration = when {
            todayCount == goal.maxCigarettesPerDay -> GoalCelebrationKind.ReachedCapHold
            todayCount == 0 && yesterdayCompleted -> GoalCelebrationKind.YesterdayUnderCap
            else -> null
        }

        return GoalProgress(
            goal = goal,
            titleKind = GoalTitleKind.DailyCap,
            target = GoalTargetSpec.DailyCap(goal.maxCigarettesPerDay),
            progress = GoalProgressSpec.DailyCap(todayCount, goal.maxCigarettesPerDay),
            supporting = when (status) {
                GoalStatus.OnTrack ->
                    if (warning == GoalWarningKind.OneMoreBreaksCap) GoalSupportingSpec.CapOneMoreBreaks
                    else GoalSupportingSpec.CapRemaining(remaining)
                GoalStatus.Completed -> GoalSupportingSpec.CapReachedHold
                GoalStatus.OffTrack -> GoalSupportingSpec.CapExceeded
                GoalStatus.NotEnoughData -> GoalSupportingSpec.None
            },
            status = status,
            progressFraction = (todayCount.toFloat() / goal.maxCigarettesPerDay.toFloat()).coerceIn(0f, 1f),
            warning = warning,
            celebration = celebration,
            streakDays = streakDays,
            isBroken = todayCount > goal.maxCigarettesPerDay,
        )
    }

    private fun evaluateReductionVsPreviousWeek(
        goal: SmokingGoal.ReductionVsPreviousWeek,
        smokes: List<Smoke>,
        preferences: UserPreferences,
        now: Instant,
    ): GoalProgress {
        val currentStart = currentWeekStartInstant(
            now = now,
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val currentEnd = nextWeekStartInstant(
            now = now,
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val previousStart = currentStart.minus(7, DateTimeUnit.DAY, timeZone)
        val previousEnd = currentStart
        return evaluateReduction(
            titleKind = GoalTitleKind.ReductionWeek,
            goal = goal,
            currentCount = smokes.count { it.date >= currentStart && it.date < currentEnd },
            baselineCount = smokes.count { it.date >= previousStart && it.date < previousEnd },
            baseline = GoalBaselineKind.PreviousWeek,
        )
    }

    private fun evaluateReductionVsPreviousMonth(
        goal: SmokingGoal.ReductionVsPreviousMonth,
        smokes: List<Smoke>,
        preferences: UserPreferences,
        now: Instant,
    ): GoalProgress {
        val currentStart = currentMonthStartInstant(
            now = now,
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val currentEnd = nextMonthStartInstant(
            now = now,
            timeZone = timeZone,
            dayStartHour = preferences.dayStartHour,
            manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
        )
        val previousStart = currentStart.minus(1, DateTimeUnit.MONTH, timeZone)
        val previousEnd = currentStart
        return evaluateReduction(
            titleKind = GoalTitleKind.ReductionMonth,
            goal = goal,
            currentCount = smokes.count { it.date >= currentStart && it.date < currentEnd },
            baselineCount = smokes.count { it.date >= previousStart && it.date < previousEnd },
            baseline = GoalBaselineKind.PreviousMonth,
        )
    }

    private fun evaluateReduction(
        titleKind: GoalTitleKind,
        goal: SmokingGoal,
        currentCount: Int,
        baselineCount: Int,
        baseline: GoalBaselineKind,
    ): GoalProgress {
        val reductionPercent = when (goal) {
            is SmokingGoal.ReductionVsPreviousWeek -> goal.reductionPercent
            is SmokingGoal.ReductionVsPreviousMonth -> goal.reductionPercent
            else -> 0.0
        }
        if (baselineCount <= 0) {
            return GoalProgress(
                goal = goal,
                titleKind = titleKind,
                target = GoalTargetSpec.ReduceByPercent(reductionPercent.toWholeOrOneDecimal()),
                progress = GoalProgressSpec.WaitingBaseline,
                baseline = baseline,
                supporting = GoalSupportingSpec.ReduceNeedBaseline,
                status = GoalStatus.NotEnoughData,
                progressFraction = null,
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
            titleKind = titleKind,
            target = GoalTargetSpec.SmokesOrFewer(targetCount.toWholeOrOneDecimal()),
            progress = GoalProgressSpec.CurrentVsBaseline(currentCount, baselineCount),
            baseline = baseline,
            supporting = when (status) {
                GoalStatus.Completed -> GoalSupportingSpec.ReduceBelowTarget
                GoalStatus.OnTrack -> GoalSupportingSpec.ReduceMovingRight
                GoalStatus.OffTrack -> GoalSupportingSpec.ReduceStillAbove
                GoalStatus.NotEnoughData -> GoalSupportingSpec.None
            },
            status = status,
            progressFraction = ((currentReduction / reductionPercent).coerceAtLeast(0.0).coerceIn(0.0, 1.0)).toFloat(),
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
            titleKind = GoalTitleKind.MindfulGap,
            target = GoalTargetSpec.WaitBetween(goal.targetMinutes.formatMinutesLabel()),
            progress = GoalProgressSpec.CurrentGap(elapsedMinutes.formatMinutesLabel()),
            supporting = when (status) {
                GoalStatus.Completed -> GoalSupportingSpec.GapMeetsTarget
                GoalStatus.OnTrack -> GoalSupportingSpec.GapBuilding
                GoalStatus.OffTrack -> GoalSupportingSpec.GapStillShort
                GoalStatus.NotEnoughData -> GoalSupportingSpec.None
            },
            status = status,
            progressFraction = (elapsedMinutes.toFloat() / goal.targetMinutes.toFloat()).coerceIn(0f, 1f),
        )
    }
}

private fun Map<LocalDate, Int>.consecutiveCompletedDays(
    endDateInclusive: LocalDate,
    maxPerDay: Int,
): Int {
    val firstTrackedDate = keys.minOrNull() ?: return 0
    var streak = 0
    var cursor = endDateInclusive
    while (cursor >= firstTrackedDate) {
        val count = this[cursor] ?: 0
        if (count > maxPerDay) break
        streak += 1
        cursor = cursor.minus(1, DateTimeUnit.DAY)
    }
    return streak
}

fun goalDataFetchStart(
    preferences: UserPreferences,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant = currentMonthStartInstant(
    now = now,
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
