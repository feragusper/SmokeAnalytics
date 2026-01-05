package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeElapsedSinceNow

/**
 * Represents the result of aggregating smoke event counts over different time periods.
 *
 * This data class encapsulates information about smoke events, including counts for today,
 * the current week, the current month, and details about the last smoke event.
 *
 * @property todaysSmokes A list of smoke events that occurred today.
 * @property countByWeek The total number of smoke events this week.
 * @property countByMonth The total number of smoke events this month.
 * @property lastSmoke The most recent smoke event, if any.
 */
data class SmokeCountListResult(
    val todaysSmokes: List<Smoke>,
    val countByWeek: Int,
    val countByMonth: Int,
    val lastSmoke: Smoke?,
) {

    /**
     * The total number of smoke events today.
     */
    val countByToday: Int = todaysSmokes.size

    /**
     * The time elapsed since the last cigarette, represented as a pair of hours and minutes.
     *
     * This property is computed lazily to avoid unnecessary calculations.
     * If no last smoke event is available, it defaults to (0L, 0L).
     */
    val timeSinceLastCigarette: Pair<Long, Long> by lazy {
        lastSmoke?.date?.timeElapsedSinceNow() ?: (0L to 0L)
    }

}
