package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke

data class SmokeCountListResult(
    val todaysSmokes: List<Smoke>,
    val countByWeek: Int,
    val countByMonth: Int,
    val lastSmoke: Smoke?,
) {
    val countByToday = todaysSmokes.size
    val timeSinceLastCigarette: Pair<Long, Long> by lazy {
        lastSmoke?.date?.timeElapsedSinceNow() ?: (0L to 0L)
    }

}

