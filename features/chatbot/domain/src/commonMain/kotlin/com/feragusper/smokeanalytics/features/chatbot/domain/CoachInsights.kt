package com.feragusper.smokeanalytics.features.chatbot.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.isInCurrentDayBucket
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlin.math.max

data class CoachContext(
    val name: String,
    val recentSmokes: List<Smoke>,
    val todayCount: Int,
    val totalCount: Int,
    val hoursSinceLastSmoke: Long,
    val minutesSinceLastSmoke: Long,
    val currentStreakHours: Long,
    val longestStreakHours: Long,
)

fun buildCoachContext(
    name: String,
    recentSmokes: List<Smoke>,
): CoachContext {
    val latest = recentSmokes.firstOrNull()
    val elapsed = latest?.date.timeElapsedSinceNow()
    val todayCount = recentSmokes.count { smoke ->
        smoke.date.isInCurrentDayBucket()
    }
    val intervals = recentSmokes.map { smoke ->
        val (hours, minutes) = smoke.timeElapsedSincePreviousSmoke
        max(0, (hours * 60 + minutes).toInt())
    }
    val longestMinutes = intervals.maxOrNull() ?: 0

    return CoachContext(
        name = name,
        recentSmokes = recentSmokes,
        todayCount = todayCount,
        totalCount = recentSmokes.size,
        hoursSinceLastSmoke = elapsed.first,
        minutesSinceLastSmoke = elapsed.second,
        currentStreakHours = elapsed.first,
        longestStreakHours = longestMinutes / 60L,
    )
}

fun fallbackInitialCoachMessage(context: CoachContext): String {
    val firstLine = when {
        context.todayCount == 0L.toInt() && context.totalCount == 0 -> "No smokes logged yet. Start by tracking honestly, not perfectly."
        context.hoursSinceLastSmoke >= 8 -> "Strong start. You're already putting real space between cigarettes."
        context.todayCount <= 3 -> "You are keeping the day under control. Protect the next few hours."
        else -> "Today's pattern is visible now. The next decision matters more than the last one."
    }

    val secondLine = when {
        context.hoursSinceLastSmoke >= 4 -> "Try to stretch this streak a little more before the next one."
        context.minutesSinceLastSmoke < 45 -> "If this is a craving window, wait ten minutes and do something physical first."
        else -> "Aim for one more delayed cigarette today rather than a perfect day."
    }

    return "$firstLine $secondLine"
}

fun fallbackCoachReply(
    message: String,
    context: CoachContext,
): String {
    val normalized = message.lowercase()

    return when {
        normalized.contains("stress") || normalized.contains("ans") || normalized.contains("nerv") ->
            "If stress is driving this, don't negotiate with the cigarette yet. Take a short walk, water, and give it ten minutes. You've already gone ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m since the last one."

        normalized.contains("crav") || normalized.contains("want") || normalized.contains("need") ->
            "Cravings peak and fall. Delay the next cigarette by ten minutes, then decide again. The goal is not magic willpower, it's breaking the automatic loop."

        normalized.contains("progress") || normalized.contains("doing") || normalized.contains("how") ->
            "Today you're at ${context.todayCount} smokes, with ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m since the last one. The clean win is to make the next gap longer than the previous one."

        normalized.contains("slip") || normalized.contains("smoked") || normalized.contains("failed") ->
            "One cigarette is data, not defeat. Log it, reset cleanly, and focus on the next interval. The app is for recovery, not punishment."

        else ->
            "Use the coach for cravings, planning, and pattern checks. Right now the best move is simple: delay the next cigarette and protect your current gap of ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m."
    }
}
