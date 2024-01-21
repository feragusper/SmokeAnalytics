package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isThisWeek
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isToday
import javax.inject.Inject

class FetchSmokeCountListUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
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
