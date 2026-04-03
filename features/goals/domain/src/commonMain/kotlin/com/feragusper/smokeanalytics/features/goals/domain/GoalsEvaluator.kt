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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
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
        val warningLabel = when {
            todayCount == goal.maxCigarettesPerDay - 1 -> "One more cigarette breaks today's cap."
            todayCount > goal.maxCigarettesPerDay -> "Today's cap is broken."
            else -> null
        }
        val celebrationLabel = when {
            todayCount == goal.maxCigarettesPerDay -> "You reached today's cap without going over. Hold here to keep the win."
            todayCount == 0 && yesterdayCompleted -> "Yesterday stayed under your cap. Strong work."
            else -> null
        }

        return GoalProgress(
            goal = goal,
            title = "Daily cap",
            targetLabel = "Target: at most ${goal.maxCigarettesPerDay} today",
            progressLabel = "$todayCount / ${goal.maxCigarettesPerDay} smoked today",
            supportingText = when (status) {
                GoalStatus.OnTrack -> warningLabel ?: "$remaining left before reaching today's cap."
                GoalStatus.Completed -> celebrationLabel ?: "You reached today's cap. Holding here keeps the goal intact."
                GoalStatus.OffTrack -> "Today's cap is exceeded. The next win is stopping the count from climbing further."
                GoalStatus.NotEnoughData -> ""
            },
            status = status,
            progressFraction = (todayCount.toFloat() / goal.maxCigarettesPerDay.toFloat()).coerceIn(0f, 1f),
            warningLabel = warningLabel,
            celebrationLabel = celebrationLabel,
            streakDays = streakDays,
            streakLabel = streakDays.takeIf { it > 0 }?.let(::formatStreakLabel),
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

private fun formatStreakLabel(days: Int): String =
    if (days == 1) "1 day completed in a row" else "$days days completed in a row"

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
