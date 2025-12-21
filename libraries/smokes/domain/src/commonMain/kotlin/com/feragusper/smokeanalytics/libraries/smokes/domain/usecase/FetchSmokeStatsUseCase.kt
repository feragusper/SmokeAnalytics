package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

class FetchSmokeStatsUseCase(
    private val smokeRepository: SmokeRepository,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {

    suspend operator fun invoke(
        year: Int,
        month: Int,
        day: Int?,
        periodType: PeriodType
    ): SmokeStats {
        val (start, endExclusive) = when (periodType) {
            PeriodType.DAY -> {
                requireNotNull(day) { "day is required for PeriodType.DAY" }
                val date = LocalDate(year, month, day)
                date.atStartOfDayIn(timeZone) to date.plus(DatePeriod(days = 1))
                    .atStartOfDayIn(timeZone)
            }

            PeriodType.WEEK -> {
                requireNotNull(day) { "day is required for PeriodType.WEEK" }
                val date = LocalDate(year, month, day)
                val startOfWeek = date.minusDaysToMonday()
                val endOfWeek = startOfWeek.plus(DatePeriod(days = 7))
                startOfWeek.atStartOfDayIn(timeZone) to endOfWeek.atStartOfDayIn(timeZone)
            }

            PeriodType.MONTH -> {
                val date = LocalDate(year, month, 1)
                val endOfMonth = date.plus(DatePeriod(months = 1))
                date.atStartOfDayIn(timeZone) to endOfMonth.atStartOfDayIn(timeZone)
            }

            PeriodType.YEAR -> {
                val date = LocalDate(year, 1, 1)
                val endOfYear = date.plus(DatePeriod(years = 1))
                date.atStartOfDayIn(timeZone) to endOfYear.atStartOfDayIn(timeZone)
            }
        }

        val smokes = smokeRepository.fetchSmokes(start, endExclusive)
        return SmokeStats.from(
            smokes = smokes,
            year = year,
            month = month,
            day = day,
            timeZone = timeZone
        )
    }

    enum class PeriodType { DAY, WEEK, MONTH, YEAR }

    private fun LocalDate.minusDaysToMonday(): LocalDate {
        // kotlinx.datetime DayOfWeek: MONDAY=1 .. SUNDAY=7
        val delta = this.dayOfWeek.isoDayNumber - 1
        return this.plus(DatePeriod(days = -delta))
    }
}