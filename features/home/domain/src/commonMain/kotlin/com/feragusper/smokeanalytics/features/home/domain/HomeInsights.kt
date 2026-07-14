package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetSnapshot
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentBucketDate
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentMonthStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentWeekStartInstant
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.time.Clock

enum class ElapsedTone {
    Urgent,
    Warning,
    Caution,
    Calm,
}

/** Time-of-day bucket for the greeting title; presentation supplies the localized words. */
enum class GreetingDayPart { Morning, Afternoon, Evening }

/** Which motivational line the greeting shows; presentation supplies the localized text. */
enum class GreetingMessage { StrongPace, KeepFirstAway, HoldingLine, OneLessCounts }

/**
 * Structured greeting: [dayPart] + [message] are localized by the presentation layer, [name]
 * is the already-resolved nickname or first name to address the user by (blank = no name).
 */
data class GreetingState(
    val dayPart: GreetingDayPart,
    val message: GreetingMessage,
    val name: String = "",
)

data class GamificationSummary(
    val currentStreakHours: Long,
    val longestStreakHours: Long,
    val points: Int,
    val nextMilestoneHours: Int,
    val badges: List<String>,
)

data class FinancialSummary(
    val spentToday: Double,
    val spentWeek: Double,
    val spentMonth: Double,
    val currencySymbol: String,
)

data class RateSummary(
    val latestIntervalMinutes: Int?,
    val averageIntervalMinutesToday: Int?,
    val averageSmokesPerDayWeek: Double,
    val averageSmokesPerDayMonth: Double,
)

/** What the current gap target represents; presentation localizes the phrase. */
enum class GapTargetKind { GoalGap, DailyCapPace, SteadyGap }

/** Gap pulse line variants; presentation localizes, domain supplies the durations. */
sealed interface GapPulseSpec {
    data object LogOrRefresh : GapPulseSpec
    data object StayWithGap : GapPulseSpec
    data class Beyond(val durationLabel: String, val target: GapTargetKind) : GapPulseSpec
    data class Until(val durationLabel: String, val target: GapTargetKind) : GapPulseSpec
}

/** Gap recovery line variants; presentation localizes, domain supplies the gap labels. */
sealed interface GapRecoverySpec {
    data object EachLongerGap : GapRecoverySpec
    data class MindfulBuilding(val gapLabel: String) : GapRecoverySpec
    data class DailyCapBuilding(val gapLabel: String) : GapRecoverySpec
    data class SteadyBuilding(val gapLabel: String) : GapRecoverySpec
}

data class GapFocusSummary(
    val targetMinutes: Int?,
    val progressFraction: Float?,
    val pulseSummary: GapPulseSpec,
    val recoverySummary: GapRecoverySpec,
)

fun elapsedToneFrom(hours: Long, minutes: Long): ElapsedTone =
    elapsedToneFromMinutes((hours * 60L + minutes).toInt())

fun elapsedToneFromMinutes(totalMinutes: Int): ElapsedTone = when {
    totalMinutes >= 180 -> ElapsedTone.Calm
    totalMinutes >= 90 -> ElapsedTone.Caution
    totalMinutes >= 45 -> ElapsedTone.Warning
    else -> ElapsedTone.Urgent
}

fun greetingStateFor(
    hourOfDay: Int,
    todayCount: Int,
    currentStreakHours: Long,
    nickname: String = "",
): GreetingState {
    val dayPart = when (hourOfDay) {
        in 5..11 -> GreetingDayPart.Morning
        in 12..18 -> GreetingDayPart.Afternoon
        else -> GreetingDayPart.Evening
    }

    val message = when {
        currentStreakHours >= 8 -> GreetingMessage.StrongPace
        todayCount == 0 -> GreetingMessage.KeepFirstAway
        todayCount <= 3 -> GreetingMessage.HoldingLine
        else -> GreetingMessage.OneLessCounts
    }

    return GreetingState(dayPart = dayPart, message = message, name = nickname.trim())
}

fun financialSummary(
    todayCount: Int,
    weekCount: Int,
    monthCount: Int,
    preferences: UserPreferences,
): FinancialSummary {
    val price = preferences.cigarettePrice
    return FinancialSummary(
        spentToday = todayCount * price,
        spentWeek = weekCount * price,
        spentMonth = monthCount * price,
        currencySymbol = preferences.currencySymbol,
    )
}

fun rateSummary(
    smokeCountListResult: SmokeCountListResult,
    preferences: UserPreferences,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): RateSummary {
    val targetGapMinutes = when (val count = smokeCountListResult.countByToday) {
        0 -> preferences.awakeMinutesPerDay
        else -> (preferences.awakeMinutesPerDay / count).coerceAtLeast(1)
    }
    val currentDate = currentBucketDate(
        now = now,
        timeZone = timeZone,
        dayStartHour = preferences.dayStartHour,
        manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
    )
    val weekStartDate = currentWeekStartInstant(
        now = now,
        timeZone = timeZone,
        dayStartHour = preferences.dayStartHour,
        manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
    ).toLocalDateTime(timeZone).date
    val monthStartDate = currentMonthStartInstant(
        now = now,
        timeZone = timeZone,
        dayStartHour = preferences.dayStartHour,
        manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
    ).toLocalDateTime(timeZone).date
    val elapsedWeekDays = (weekStartDate.daysUntil(currentDate) + 1).coerceAtLeast(1)
    val elapsedMonthDays = (monthStartDate.daysUntil(currentDate) + 1).coerceAtLeast(1)

    return RateSummary(
        latestIntervalMinutes = smokeCountListResult.lastSmoke
            ?.timeElapsedSincePreviousSmoke
            ?.totalMinutes()
            ?.takeIf { it > 0 },
        averageIntervalMinutesToday = targetGapMinutes,
        averageSmokesPerDayWeek = smokeCountListResult.countByWeek / elapsedWeekDays.toDouble(),
        averageSmokesPerDayMonth = smokeCountListResult.countByMonth / elapsedMonthDays.toDouble(),
    )
}

fun gapFocusSummary(
    elapsedMinutes: Long?,
    rateSummary: RateSummary?,
    goalProgress: GoalProgress?,
    smokesPerDay: Int? = null,
    awakeMinutesPerDay: Int = 0,
    dayStartHour: Int = 0,
    bedtimeHour: Int = 0,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): GapFocusSummary {
    val targetMinutes = when (val goal = goalProgress?.goal) {
        is SmokingGoal.MindfulGap -> goal.targetMinutes
        is SmokingGoal.DailyCap -> {
            val dayWindow = activeDayWindow(
                dayStartHour = dayStartHour,
                bedtimeHour = bedtimeHour,
                awakeMinutesPerDay = awakeMinutesPerDay,
                now = now,
                timeZone = timeZone,
            )
            val remaining = goal.maxCigarettesPerDay - (smokesPerDay ?: 0)
            if (remaining > 0) (dayWindow.remainingMinutes / remaining).coerceAtLeast(1) else null
        }
        else -> rateSummary?.averageIntervalMinutesToday?.takeIf { it > 0 }
    }
    val progressFraction = if (elapsedMinutes == null || targetMinutes == null || targetMinutes <= 0) {
        null
    } else {
        (elapsedMinutes.toFloat() / targetMinutes.toFloat()).coerceIn(0f, 1f)
    }
    val targetKind = when (goalProgress?.goal) {
        is SmokingGoal.MindfulGap -> GapTargetKind.GoalGap
        is SmokingGoal.DailyCap -> GapTargetKind.DailyCapPace
        else -> GapTargetKind.SteadyGap
    }

    val pulseSummary: GapPulseSpec = when {
        elapsedMinutes == null -> GapPulseSpec.LogOrRefresh
        targetMinutes == null || targetMinutes <= 0 -> GapPulseSpec.StayWithGap
        elapsedMinutes >= targetMinutes -> GapPulseSpec.Beyond((elapsedMinutes - targetMinutes).toDurationLabel(), targetKind)
        else -> GapPulseSpec.Until((targetMinutes - elapsedMinutes).toDurationLabel(), targetKind)
    }
    val recoverySummary: GapRecoverySpec = when {
        targetMinutes == null || targetMinutes <= 0 -> GapRecoverySpec.EachLongerGap
        goalProgress?.goal is SmokingGoal.MindfulGap -> GapRecoverySpec.MindfulBuilding(targetMinutes.toGapLabel())
        goalProgress?.goal is SmokingGoal.DailyCap -> GapRecoverySpec.DailyCapBuilding(targetMinutes.toGapLabel())
        else -> GapRecoverySpec.SteadyBuilding(targetMinutes.toGapLabel())
    }

    return GapFocusSummary(
        targetMinutes = targetMinutes,
        progressFraction = progressFraction,
        pulseSummary = pulseSummary,
        recoverySummary = recoverySummary,
    )
}

fun gamificationSummary(smokes: List<Smoke>): GamificationSummary {
    if (smokes.isEmpty()) {
        return GamificationSummary(
            currentStreakHours = 0,
            longestStreakHours = 0,
            points = 0,
            nextMilestoneHours = 2,
            badges = emptyList(),
        )
    }

    val intervals = smokes.map { smoke ->
        val (hours, minutes) = smoke.timeElapsedSincePreviousSmoke
        max(0, (hours * 60 + minutes).toInt())
    }

    val longestMinutes = intervals.maxOrNull() ?: 0
    val currentMinutes = intervals.firstOrNull() ?: 0
    val points = intervals.sumOf { it / 15 }
    val badges = buildList {
        if (longestMinutes >= 120) add("2h")
        if (longestMinutes >= 240) add("4h")
        if (longestMinutes >= 480) add("8h")
        if (longestMinutes >= 720) add("12h")
    }
    val milestones = listOf(2, 4, 8, 12, 24, 48)
    val next = milestones.firstOrNull { it * 60 > currentMinutes } ?: 72

    return GamificationSummary(
        currentStreakHours = currentMinutes / 60L,
        longestStreakHours = longestMinutes / 60L,
        points = points,
        nextMilestoneHours = next,
        badges = badges,
    )
}

fun SmokeCountListResult.toWidgetSnapshot(
    preferences: UserPreferences,
    goalProgress: GoalProgress? = null,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): WidgetSnapshot {
    val rate = rateSummary(
        smokeCountListResult = this,
        preferences = preferences,
        now = now,
        timeZone = timeZone,
    )
    val elapsed = timeSinceLastCigarette
    val elapsedMinutes = elapsed.first * 60L + elapsed.second
    val gapFocus = gapFocusSummary(
        elapsedMinutes = elapsedMinutes,
        rateSummary = rate,
        goalProgress = goalProgress,
        smokesPerDay = countByToday,
        awakeMinutesPerDay = preferences.awakeMinutesPerDay,
        dayStartHour = preferences.dayStartHour,
        bedtimeHour = preferences.bedtimeHour,
        now = now,
        timeZone = timeZone,
    )
    return WidgetSnapshot(
        todayCount = countByToday,
        elapsedHours = elapsed.first,
        elapsedMinutes = elapsed.second,
        targetGapMinutes = gapFocus.targetMinutes ?: rate.averageIntervalMinutesToday ?: preferences.awakeMinutesPerDay,
        averageSmokesPerDayWeek = rate.averageSmokesPerDayWeek,
    )
}

private fun Pair<Long, Long>.totalMinutes(): Int = (first * 60L + second).toInt()

private fun Long.toDurationLabel(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours <= 0 -> "${minutes}m"
        minutes == 0L -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}

private fun Int.toGapLabel(): String = when {
    this >= 60 -> {
        val hours = this / 60
        val minutes = this % 60
        if (minutes == 0) "${hours}h" else "${hours}h ${minutes}m"
    }
    else -> "${this}m"
}
