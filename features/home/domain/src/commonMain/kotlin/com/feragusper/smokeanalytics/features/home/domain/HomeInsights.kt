package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.WidgetSnapshot
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlin.math.max

enum class ElapsedTone {
    Urgent,
    Caution,
    Calm,
}

data class GreetingState(
    val title: String,
    val message: String,
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
)

fun elapsedToneFrom(hours: Long, minutes: Long): ElapsedTone = when {
    hours >= 6 -> ElapsedTone.Calm
    hours >= 2 || minutes >= 45 -> ElapsedTone.Caution
    else -> ElapsedTone.Urgent
}

fun greetingStateFor(hourOfDay: Int, todayCount: Int, currentStreakHours: Long): GreetingState {
    val title = when (hourOfDay) {
        in 5..11 -> "Good morning"
        in 12..18 -> "Good afternoon"
        else -> "Good evening"
    }

    val message = when {
        currentStreakHours >= 8 -> "Strong pace today."
        todayCount == 0 -> "Keep the first one away."
        todayCount <= 3 -> "You are holding the line."
        else -> "One less still counts."
    }

    return GreetingState(title = title, message = message)
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

fun SmokeCountListResult.toWidgetSnapshot(preferences: UserPreferences): WidgetSnapshot {
    val financial = financialSummary(
        todayCount = countByToday,
        weekCount = countByWeek,
        monthCount = countByMonth,
        preferences = preferences,
    )
    val gamification = gamificationSummary(todaysSmokes)
    val elapsed = timeSinceLastCigarette
    return WidgetSnapshot(
        todayCount = countByToday,
        weekCount = countByWeek,
        monthCount = countByMonth,
        currentStreakHours = gamification.currentStreakHours,
        elapsedHours = elapsed.first,
        elapsedMinutes = elapsed.second,
        spentToday = financial.spentToday,
    )
}
