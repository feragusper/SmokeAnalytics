package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.features.goals.domain.GoalProgress
import com.feragusper.smokeanalytics.features.goals.domain.GoalStatus
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.absoluteValue

data class HomeGoalNarrative(
    val heroTitle: String,
    val heroSupporting: String,
    val statusLabel: String,
    val consistencyLabel: String,
    val nextActionLabel: String,
)

enum class HomeHeroProgressTone {
    Green,
    Yellow,
    Red,
    Neutral,
}

data class HomeHeroProgress(
    val fraction: Float,
    val tone: HomeHeroProgressTone,
)

data class HomeDebugBlock(
    val title: String,
    val lines: List<String>,
)

data class ActiveDayWindow(
    val elapsedMinutes: Int,
    val remainingMinutes: Int,
)

fun homeGoalNarrative(
    goalProgress: GoalProgress?,
    smokesPerDay: Int?,
    timeSinceLastCigarette: Pair<Long, Long>? = null,
    awakeMinutesPerDay: Int = 0,
    dayStartHour: Int = 0,
    bedtimeHour: Int = 0,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): HomeGoalNarrative {
    if (goalProgress == null) {
        return HomeGoalNarrative(
            heroTitle = "Set one goal for today",
            heroSupporting = "A visible goal keeps Home anchored in the part that matters most.",
            statusLabel = "No active goal",
            consistencyLabel = "Add a daily cap, reduction target, or mindful gap to make the day easier to read.",
            nextActionLabel = "Set your goal in You",
        )
    }

    return when (val goal = goalProgress.goal) {
        is SmokingGoal.DailyCap -> {
            val smokedToday = smokesPerDay ?: 0
            val remaining = goal.maxCigarettesPerDay - smokedToday
            val dayWindow = activeDayWindow(
                dayStartHour = dayStartHour,
                bedtimeHour = bedtimeHour,
                awakeMinutesPerDay = awakeMinutesPerDay,
                now = now,
                timeZone = timeZone,
            )
            val dynamicGapMinutes = dailyCapRemainingGapMinutes(
                remainingCigarettes = remaining,
                remainingActiveMinutes = dayWindow.remainingMinutes,
            )
            val expectedSmokesByNow = expectedSmokesByNow(
                elapsedActiveMinutes = dayWindow.elapsedMinutes,
                awakeMinutesPerDay = awakeMinutesPerDay,
                dailyCap = goal.maxCigarettesPerDay,
            )
            val pacingMessage = when {
                remaining < 0 -> "You're beyond today's cap. The next win is holding the count here."
                remaining == 0 -> "You've used today's cap. Holding here keeps the day intact."
                smokedToday <= expectedSmokesByNow -> "You're pacing well for today's cap."
                dynamicGapMinutes != null -> "You need roughly ${dynamicGapMinutes.toDurationLabel()} between the remaining cigarettes to stay within today's cap."
                else -> "You're smoking faster than the pace that keeps today's cap intact."
            }
            HomeGoalNarrative(
                heroTitle = when {
                    remaining > 0 -> "$remaining ${if (remaining == 1) "cigarette" else "cigarettes"} left today"
                    remaining == 0 -> "0 cigarettes left today"
                    else -> "${remaining.absoluteValue} over today's cap"
                },
                heroSupporting = pacingMessage,
                statusLabel = goalProgress.status.toHomeStatusLabel(),
                consistencyLabel = goalProgress.streakLabel ?: when (goalProgress.status) {
                    GoalStatus.OnTrack -> "Still within today's cap."
                    GoalStatus.Completed -> "You've reached the cap. Holding here keeps the day intact."
                    GoalStatus.OffTrack -> "Pause the count here to steady the rest of the day."
                    GoalStatus.NotEnoughData -> "Waiting for enough data to judge today's pace."
                },
                nextActionLabel = if (goalProgress.isBroken) "Review today's cap in You" else "Keep today's cap visible in You",
            )
        }

        is SmokingGoal.MindfulGap -> HomeGoalNarrative(
            heroTitle = "Wait ${goal.targetMinutes.toDurationLabel()} before the next cigarette",
            heroSupporting = goalProgress.supportingText,
            statusLabel = goalProgress.status.toHomeStatusLabel(),
            consistencyLabel = goalProgress.streakLabel ?: when (goalProgress.status) {
                GoalStatus.OnTrack -> "The current gap is building in the right direction."
                GoalStatus.Completed -> "Latest gap meets the target."
                GoalStatus.OffTrack -> "A few more minutes will change the shape of this gap."
                GoalStatus.NotEnoughData -> "Waiting for enough data to judge the gap."
            },
            nextActionLabel = "Review your mindful gap in You",
        )

        is SmokingGoal.ReductionVsPreviousWeek -> HomeGoalNarrative(
            heroTitle = "Reduce by ${goal.reductionPercent.toCompactPercent()} this week",
            heroSupporting = goalProgress.supportingText,
            statusLabel = goalProgress.status.toHomeStatusLabel(),
            consistencyLabel = goalProgress.streakLabel ?: reductionConsistency(goalProgress),
            nextActionLabel = "Review this week's target in You",
        )

        is SmokingGoal.ReductionVsPreviousMonth -> HomeGoalNarrative(
            heroTitle = "Reduce by ${goal.reductionPercent.toCompactPercent()} this month",
            heroSupporting = goalProgress.supportingText,
            statusLabel = goalProgress.status.toHomeStatusLabel(),
            consistencyLabel = goalProgress.streakLabel ?: reductionConsistency(goalProgress),
            nextActionLabel = "Review this month's target in You",
        )
    }
}

fun homeHeroProgress(
    goalProgress: GoalProgress?,
    smokesPerDay: Int?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    awakeMinutesPerDay: Int,
    dayStartHour: Int = 0,
    bedtimeHour: Int = 0,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): HomeHeroProgress {
    val goal = goalProgress?.goal
    if (goal is SmokingGoal.DailyCap) {
        val smokedToday = smokesPerDay ?: 0
        val dayWindow = activeDayWindow(
            dayStartHour = dayStartHour,
            bedtimeHour = bedtimeHour,
            awakeMinutesPerDay = awakeMinutesPerDay,
            now = now,
            timeZone = timeZone,
        )
        val expectedSmokesByNow = expectedSmokesByNow(
            elapsedActiveMinutes = dayWindow.elapsedMinutes,
            awakeMinutesPerDay = awakeMinutesPerDay,
            dailyCap = goal.maxCigarettesPerDay,
        )

        return when {
            smokedToday > goal.maxCigarettesPerDay -> {
                val deviation = ((smokedToday - goal.maxCigarettesPerDay).toFloat() / goal.maxCigarettesPerDay.toFloat())
                    .coerceIn(0f, 1f)
                    .coerceAtLeast(0.08f)
                HomeHeroProgress(
                    fraction = deviation,
                    tone = HomeHeroProgressTone.Red,
                )
            }

            smokedToday <= expectedSmokesByNow -> HomeHeroProgress(
                fraction = 1f,
                tone = HomeHeroProgressTone.Green,
            )

            else -> HomeHeroProgress(
                fraction = (expectedSmokesByNow / smokedToday.toFloat()).coerceIn(0.08f, 1f),
                tone = HomeHeroProgressTone.Yellow,
            )
        }
    }

    return HomeHeroProgress(
        fraction = goalProgress?.progressFraction?.coerceIn(0.08f, 1f) ?: 0.16f,
        tone = when (goalProgress?.status) {
            GoalStatus.Completed, GoalStatus.OnTrack -> HomeHeroProgressTone.Green
            GoalStatus.OffTrack -> HomeHeroProgressTone.Yellow
            GoalStatus.NotEnoughData, null -> HomeHeroProgressTone.Neutral
        },
    )
}

fun homeHeroDebugBlock(
    goalProgress: GoalProgress?,
    smokesPerDay: Int?,
    timeSinceLastCigarette: Pair<Long, Long>?,
    awakeMinutesPerDay: Int,
    dayStartHour: Int = 0,
    bedtimeHour: Int = 0,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): HomeDebugBlock? {
    val elapsedMinutes = timeSinceLastCigarette?.let { it.first * 60 + it.second }
    val heroProgress = homeHeroProgress(
        goalProgress = goalProgress,
        smokesPerDay = smokesPerDay,
        timeSinceLastCigarette = timeSinceLastCigarette,
        awakeMinutesPerDay = awakeMinutesPerDay,
        dayStartHour = dayStartHour,
        bedtimeHour = bedtimeHour,
        now = now,
        timeZone = timeZone,
    )

    return when (val goal = goalProgress?.goal) {
        is SmokingGoal.DailyCap -> {
            val smokedToday = smokesPerDay ?: 0
            val remaining = goal.maxCigarettesPerDay - smokedToday
            val dayWindow = activeDayWindow(
                dayStartHour = dayStartHour,
                bedtimeHour = bedtimeHour,
                awakeMinutesPerDay = awakeMinutesPerDay,
                now = now,
                timeZone = timeZone,
            )
            val expectedGapMinutes = (awakeMinutesPerDay / goal.maxCigarettesPerDay).coerceAtLeast(1)
            val expectedByNow = expectedSmokesByNow(
                elapsedActiveMinutes = dayWindow.elapsedMinutes,
                awakeMinutesPerDay = awakeMinutesPerDay,
                dailyCap = goal.maxCigarettesPerDay,
            )
            val remainingGapMinutes = dailyCapRemainingGapMinutes(
                remainingCigarettes = remaining,
                remainingActiveMinutes = dayWindow.remainingMinutes,
            )
            val overCapBy = (smokedToday - goal.maxCigarettesPerDay).coerceAtLeast(0)
            val rawDeviationFraction = if (overCapBy > 0) {
                overCapBy.toFloat() / goal.maxCigarettesPerDay.toFloat()
            } else {
                null
            }
            HomeDebugBlock(
                title = "Debug: goal pace",
                lines = buildList {
                    add("awakeMinutesPerDay = $awakeMinutesPerDay")
                    add("dailyCap = ${goal.maxCigarettesPerDay}")
                    add("baseExpectedGap = awakeMinutesPerDay / dailyCap = $awakeMinutesPerDay / ${goal.maxCigarettesPerDay} = ${expectedGapMinutes}m")
                    add("activeElapsedMinutes = ${dayWindow.elapsedMinutes}m")
                    add("activeRemainingMinutes = ${dayWindow.remainingMinutes}m")
                    add("elapsedGap = ${elapsedMinutes.toDebugMinutesLabel()}")
                    add("smokedToday = $smokedToday")
                    add("remaining = ${goal.maxCigarettesPerDay} - $smokedToday = $remaining")
                    add("expectedSmokesByNow = (activeElapsedMinutes / awakeMinutesPerDay) * dailyCap = (${dayWindow.elapsedMinutes} / $awakeMinutesPerDay) * ${goal.maxCigarettesPerDay} = ${expectedByNow.toInt()}")
                    add("remainingGapTarget = activeRemainingMinutes / remaining = ${dayWindow.remainingMinutes} / ${remaining.coerceAtLeast(1)} = ${remainingGapMinutes?.let { "${it}m" } ?: "null"}")
                    add("paceRatio = expectedSmokesByNow / smokedToday = ${expectedByNow.toInt()} / $smokedToday = ${if (smokedToday > 0) ((expectedByNow / smokedToday.toFloat()) * 100).toInt() else 100}%")
                    if (rawDeviationFraction != null) {
                        add("deviation = overCap / dailyCap = $overCapBy / ${goal.maxCigarettesPerDay} = ${(rawDeviationFraction * 100).toInt()}%")
                    }
                    add("heroFractionShown = ${(heroProgress.fraction * 100).toInt()}%")
                    add("heroTone = ${heroProgress.tone.name}")
                },
            )
        }

        is SmokingGoal.MindfulGap -> HomeDebugBlock(
            title = "Debug: goal pace",
            lines = buildList {
                add("goalType = MindfulGap")
                add("targetGap = ${goal.targetMinutes}m")
                add("elapsedGap = ${elapsedMinutes.toDebugMinutesLabel()}")
                add("goalProgressFractionShown = ${(heroProgress.fraction * 100).toInt()}%")
                add("heroTone = ${heroProgress.tone.name}")
            },
        )

        else -> null
    }
}

fun gapFocusDebugBlock(
    elapsedMinutes: Long?,
    rateSummary: RateSummary?,
    goalProgress: GoalProgress?,
    gapFocus: GapFocusSummary,
): HomeDebugBlock {
    val targetSource = when (goalProgress?.goal) {
        is SmokingGoal.MindfulGap -> "mindful-gap-goal"
        is SmokingGoal.DailyCap -> "daily-cap-goal"
        else -> "rateSummary.averageIntervalMinutesToday"
    }
    val deltaMinutes = when {
        elapsedMinutes == null || gapFocus.targetMinutes == null -> null
        else -> elapsedMinutes - gapFocus.targetMinutes.toLong()
    }
    return HomeDebugBlock(
        title = "Debug: current gap",
        lines = buildList {
            add("elapsedGap = ${elapsedMinutes.toDebugMinutesLabel()}")
            add("targetSource = $targetSource")
            add("targetGap = ${gapFocus.targetMinutes.toDebugMinutesLabel()}")
            add("rateSummary.averageIntervalMinutesToday = ${rateSummary?.averageIntervalMinutesToday.toDebugMinutesLabel()}")
            add("rateSummary.latestIntervalMinutes = ${rateSummary?.latestIntervalMinutes.toDebugMinutesLabel()}")
            if (elapsedMinutes != null && gapFocus.targetMinutes != null && gapFocus.targetMinutes > 0) {
                add("gapRatio = elapsedGap / targetGap = $elapsedMinutes / ${gapFocus.targetMinutes} = ${((elapsedMinutes.toFloat() / gapFocus.targetMinutes.toFloat()) * 100).toInt()}%")
            }
            add("progressShown = ${gapFocus.progressFraction?.toPercentLabel() ?: "null"}")
            add("delta = ${deltaMinutes.toSignedDebugMinutesLabel()}")
            add("summary = ${gapFocus.pulseSummaryText}")
        },
    )
}

fun Pair<Long, Long>?.toElapsedGapLabel(): String = this?.let { (hours, minutes) ->
    when {
        hours <= 0L -> "${minutes}m"
        minutes <= 0L -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
} ?: "--"

fun Instant.toHomeClockLabel(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val time = toLocalDateTime(timeZone).time
    return "${time.hour.toTwoDigits()}:${time.minute.toTwoDigits()}"
}

private fun reductionConsistency(goalProgress: GoalProgress): String =
    when (goalProgress.status) {
        GoalStatus.OnTrack -> "The current pace is moving in the right direction."
        GoalStatus.Completed -> "The current pace is already below the target."
        GoalStatus.OffTrack -> "A steadier stretch is needed to bring this reduction back on track."
        GoalStatus.NotEnoughData -> "A previous comparison window is needed before this goal can be judged."
    }

private fun GoalStatus.toHomeStatusLabel(): String = when (this) {
    GoalStatus.OnTrack -> "On track"
    GoalStatus.OffTrack -> "At risk"
    GoalStatus.Completed -> "Goal met"
    GoalStatus.NotEnoughData -> "Needs baseline"
}

private fun Double.toCompactPercent(): String {
    val rounded = (this * 10).toInt() / 10.0
    return if (rounded % 1.0 == 0.0) "${rounded.toInt()}%" else "$rounded%"
}

private fun Int.toDurationLabel(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours <= 0 -> "${minutes}m"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}

private fun Int.toTwoDigits(): String = toString().padStart(2, '0')

fun activeDayWindow(
    dayStartHour: Int,
    bedtimeHour: Int,
    awakeMinutesPerDay: Int,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): ActiveDayWindow {
    if (dayStartHour !in 0..23 || bedtimeHour !in 0..23 || awakeMinutesPerDay <= 0) {
        return ActiveDayWindow(elapsedMinutes = 0, remainingMinutes = awakeMinutesPerDay.coerceAtLeast(0))
    }
    val localTime = now.toLocalDateTime(timeZone).time
    val currentMinutes = localTime.hour * 60 + localTime.minute
    val dayStartMinutes = dayStartHour * 60
    val bedtimeMinutes = bedtimeHour * 60

    if (bedtimeHour > dayStartHour) {
        return when {
            currentMinutes <= dayStartMinutes -> ActiveDayWindow(0, awakeMinutesPerDay)
            currentMinutes >= bedtimeMinutes -> ActiveDayWindow(awakeMinutesPerDay, 0)
            else -> {
                val elapsed = currentMinutes - dayStartMinutes
                ActiveDayWindow(elapsed, (awakeMinutesPerDay - elapsed).coerceAtLeast(0))
            }
        }
    }

    val minutesSinceStart = (currentMinutes - dayStartMinutes).mod(24 * 60)
    return if (minutesSinceStart >= awakeMinutesPerDay) {
        ActiveDayWindow(awakeMinutesPerDay, 0)
    } else {
        ActiveDayWindow(minutesSinceStart, (awakeMinutesPerDay - minutesSinceStart).coerceAtLeast(0))
    }
}

private fun expectedSmokesByNow(
    elapsedActiveMinutes: Int,
    awakeMinutesPerDay: Int,
    dailyCap: Int,
): Float {
    if (awakeMinutesPerDay <= 0 || dailyCap <= 0) return 0f
    return (elapsedActiveMinutes.toFloat() / awakeMinutesPerDay.toFloat()) * dailyCap.toFloat()
}

private fun dailyCapRemainingGapMinutes(
    remainingCigarettes: Int,
    remainingActiveMinutes: Int,
): Int? {
    if (remainingCigarettes <= 0 || remainingActiveMinutes <= 0) return null
    return (remainingActiveMinutes / remainingCigarettes).coerceAtLeast(1)
}

private fun Long?.toDebugMinutesLabel(): String = this?.let { "${it}m" } ?: "null"

private fun Int?.toDebugMinutesLabel(): String = this?.let { "${it}m" } ?: "null"

private fun Float.toPercentLabel(): String = "${(this * 100).toInt()}%"

private fun Long?.toSignedDebugMinutesLabel(): String = when {
    this == null -> "null"
    this >= 0 -> "+${this}m"
    else -> "${this}m"
}
