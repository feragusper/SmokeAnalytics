package com.feragusper.smokeanalytics.libraries.cravings.domain

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome

/**
 * Reward points granted for resolving a craving.
 *
 * The user earns points for the minutes they managed to wait, plus a flat bonus
 * for fully resisting (letting the urge pass without smoking). Giving in without
 * waiting earns nothing.
 */
object CravingReward {

    /** Points per minute waited. */
    const val POINTS_PER_MINUTE_DIVISOR = 5

    /** Flat bonus added when the urge is fully resisted. */
    const val RESIST_BONUS = 10

    fun pointsFor(outcome: CravingOutcome, waitedMinutes: Int): Int {
        val minutes = waitedMinutes.coerceAtLeast(0)
        return when (outcome) {
            CravingOutcome.RESISTED -> minutes / POINTS_PER_MINUTE_DIVISOR + RESIST_BONUS
            CravingOutcome.POSTPONED -> minutes / POINTS_PER_MINUTE_DIVISOR
            CravingOutcome.PENDING, CravingOutcome.GAVE_IN -> 0
        }
    }
}
