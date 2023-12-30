package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.timeElapsedSinceNow

data class SmokeCountListResult(
    val todaysSmokes: List<Smoke>,
    val countByWeek: Int,
    val countByMonth: Int,
) {
    val countByToday = todaysSmokes.size
    val timeSinceLastCigarette: Pair<Long, Long> = todaysSmokes.first().date.timeElapsedSinceNow()
}
