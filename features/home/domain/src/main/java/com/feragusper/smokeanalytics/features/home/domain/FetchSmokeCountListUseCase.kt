package com.feragusper.smokeanalytics.features.home.domain

import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isThisWeek
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.isToday
import javax.inject.Inject

class FetchSmokeCountListUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
    suspend operator fun invoke() = smokeRepository.fetchSmokes().toSmokeCountListResult()

    private fun List<Smoke>.toSmokeCountListResult() = SmokeCountListResult(
        byToday = filterToday().size,
        byWeek = filterThisWeek().size,
        byMonth = filterThisMonth().size,
    )

    private fun List<Smoke>.filterToday() = filter { it.date.isToday() }

    private fun List<Smoke>.filterThisWeek() = filter { it.date.isThisWeek() }

    private fun List<Smoke>.filterThisMonth() = filter { it.date.isThisMonth() }

}
