package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import javax.inject.Inject

/**
 * Use case for fetching aggregated counts of smoke events over different time periods.
 * It provides the number of smokes today, this week, this month, and details about the last smoke event.
 *
 * This use case abstracts the business logic for aggregating smoke events, making it easy to call from the presentation layer.
 *
 * @property smokeRepository The repository responsible for fetching smoke event data.
 */
class FetchSmokeCountListUseCase @Inject constructor(
    private val smokeRepository: SmokeRepository
) {

    /**
     * Executes the use case to fetch and aggregate smoke event data.
     *
     * This operator function allows the use case to be invoked as a function,
     * returning a [SmokeCountListResult] containing the aggregated smoke event data.
     *
     * @return A [SmokeCountListResult] containing today's smokes, weekly and monthly counts, and details about the last smoke event.
     */
    suspend operator fun invoke() = smokeRepository.fetchSmokeCount().let {
        SmokeCountListResult(
            todaysSmokes = it.today,
            countByWeek = it.week,
            countByMonth = it.month,
            lastSmoke = it.lastSmoke,
        )
    }

}
