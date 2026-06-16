package com.feragusper.smokeanalytics.libraries.cravings.domain

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingAdvice
import com.feragusper.smokeanalytics.libraries.preferences.domain.SmokingGoal
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import kotlinx.datetime.Instant
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * Works out whether a craving should be postponed and, if so, for how long.
 *
 * The recommended gap between cigarettes is derived from the active goal:
 *  - [SmokingGoal.MindfulGap] maps directly to its target interval.
 *  - [SmokingGoal.DailyCap] is spread across the user's awake window
 *    (awake minutes / cap) to get an even pace.
 *  - Reduction goals do not pace individual cigarettes, so no wait is suggested.
 *
 * When there is no pacing goal or no previous cigarette, the user can smoke now.
 */
class CravingWaitCalculator {

    operator fun invoke(
        activeGoal: SmokingGoal?,
        lastSmokeAt: Instant?,
        preferences: UserPreferences,
        now: Instant = Clock.System.now(),
    ): CravingAdvice {
        val gapMinutes = recommendedGapMinutes(activeGoal, preferences)

        if (gapMinutes == null || lastSmokeAt == null) {
            return CravingAdvice(
                recommendedGapMinutes = gapMinutes,
                nextAllowedAt = null,
                canSmokeNow = true,
                waitMinutes = 0,
            )
        }

        val nextAllowedAt = lastSmokeAt + gapMinutes.minutes
        val remaining = nextAllowedAt - now
        val waitMinutes = if (remaining.isPositive()) {
            // Round up so the countdown never tells the user "0 left" while time remains.
            ((remaining.inWholeSeconds + 59) / 60).toInt()
        } else {
            0
        }

        return CravingAdvice(
            recommendedGapMinutes = gapMinutes,
            nextAllowedAt = nextAllowedAt,
            canSmokeNow = waitMinutes == 0,
            waitMinutes = waitMinutes,
        )
    }

    private fun recommendedGapMinutes(
        activeGoal: SmokingGoal?,
        preferences: UserPreferences,
    ): Int? = when (activeGoal) {
        null -> null
        is SmokingGoal.MindfulGap -> activeGoal.targetMinutes.coerceAtLeast(1)
        is SmokingGoal.DailyCap -> {
            val cap = activeGoal.maxCigarettesPerDay.coerceAtLeast(1)
            (preferences.awakeMinutesPerDay / cap).coerceAtLeast(1)
        }
        is SmokingGoal.ReductionVsPreviousWeek,
        is SmokingGoal.ReductionVsPreviousMonth,
        -> null
    }
}
