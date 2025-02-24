package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke

/**
 * Represents the result of aggregating smoke event counts over different time periods.
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
    val countByToday = todaysSmokes.size

    /**
     * The time elapsed since the last cigarette, represented as a pair of hours and minutes.
     */
    val timeSinceLastCigarette: Pair<Long, Long> by lazy {
        lastSmoke?.date?.timeElapsedSinceNow() ?: (0L to 0L)
    }

}

