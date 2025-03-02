package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for fetching smoke statistics for a given month.
 *
 * This use case allows retrieving aggregated data on smoking habits, including daily,
 * weekly, and monthly statistics.
 *
 * @property smokeRepository The [SmokeRepository] used for fetching smoke data.
 */
class FetchSmokeStatsUseCase @Inject constructor(
    private val smokeRepository: SmokeRepository
) {
    /**
     * Fetches the smoke statistics for a given period.
     *
     * @param year The year of the desired statistics.
     * @param month The month (1-12) if applicable.
     * @param day The day (1-31) if applicable.
     * @param periodType The type of period (day, week, month, year).
     * @return A [SmokeStats] object containing aggregated data.
     */
    suspend operator fun invoke(
        year: Int,
        month: Int,
        day: Int,
        periodType: PeriodType
    ): SmokeStats {
        val (startDate, endDate) = when (periodType) {
            PeriodType.DAY -> {
                val date = LocalDate.of(year, month, day)
                date.atStartOfDay() to date.plusDays(1).atStartOfDay()
            }

            PeriodType.WEEK -> {
                val date = LocalDate.of(year, month, day)
                val startOfWeek = date.minusDays(date.dayOfWeek.value.toLong() - 1)
                val endOfWeek = startOfWeek.plusDays(7)
                startOfWeek.atStartOfDay() to endOfWeek.atStartOfDay()
            }

            PeriodType.MONTH -> {
                val date = LocalDate.of(year, month, 1)
                val endOfMonth = date.plusMonths(1)
                date.atStartOfDay() to endOfMonth.atStartOfDay()
            }

            PeriodType.YEAR -> {
                val date = LocalDate.of(year, 1, 1)
                val endOfYear = date.plusYears(1)
                date.atStartOfDay() to endOfYear.atStartOfDay()
            }
        }

        val smokes = smokeRepository.fetchSmokes(startDate, endDate)
        return SmokeStats.from(smokes, year, month, day)
    }

    /**
     * Defines the different types of periods that can be used for statistics aggregation.
     */
    enum class PeriodType {
        DAY, WEEK, MONTH, YEAR
    }
}
