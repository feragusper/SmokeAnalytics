package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isThisWeek
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isToday
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.SmokeRepository
import javax.inject.Inject

/**
 * Use case for fetching aggregated counts of smoke events over different time periods.
 * It provides the number of smokes today, this week, this month, and details about the last smoke event.
 *
 * @property smokeRepository The repository responsible for fetching smoke event data.
 */
class FetchSmokeCountListUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {

    /**
     * Executes the use case to fetch and aggregate smoke event data.
     *
     * @return A [SmokeCountListResult] containing the aggregated smoke event data.
     */
    suspend operator fun invoke() = smokeRepository.fetchSmokes().toSmokeCountListResult()

    private fun List<Smoke>.toSmokeCountListResult() = SmokeCountListResult(
        todaysSmokes = filterToday(),
        countByWeek = filterThisWeek().size,
        countByMonth = filterThisMonth().size,
        lastSmoke = firstOrNull(),
    )

    private fun List<Smoke>.filterToday() = filter { it.date.isToday() }

    private fun List<Smoke>.filterThisWeek() = filter { it.date.isThisWeek() }

    private fun List<Smoke>.filterThisMonth() = filter { it.date.isThisMonth() }

}
