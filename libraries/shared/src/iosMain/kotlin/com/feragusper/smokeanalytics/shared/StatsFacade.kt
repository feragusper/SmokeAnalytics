package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** One bar of the daily chart: [label] is the day-of-month, [count] the cigarettes that day. */
data class StatsBar(val label: String, val count: Int)

/** A trigger/tag and how many cigarettes in the period carried it. */
data class StatsTrigger(val label: String, val count: Int)

/** Period keys shared with the Swift segmented control. */
object StatsPeriodKeys {
    const val DAY = "day"
    const val WEEK = "week"
    const val MONTH = "month"
    const val YEAR = "year"
}

/** Swift-friendly Analytics snapshot for a selected period. */
data class StatsSnapshot(
    val periodTotal: Int,
    val dailyAverage: Double,
    val bars: List<StatsBar>,
    val triggers: List<StatsTrigger>,
)

/** Swift-facing entry point for the Analytics screen. */
class StatsFacade : KoinComponent {

    private val fetchStats: FetchSmokeStatsUseCase by inject()

    @Throws(Throwable::class)
    suspend fun load(periodKey: String): StatsSnapshot {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val periodType = when (periodKey) {
            StatsPeriodKeys.DAY -> FetchSmokeStatsUseCase.PeriodType.DAY
            StatsPeriodKeys.WEEK -> FetchSmokeStatsUseCase.PeriodType.WEEK
            StatsPeriodKeys.YEAR -> FetchSmokeStatsUseCase.PeriodType.YEAR
            else -> FetchSmokeStatsUseCase.PeriodType.MONTH
        }
        val stats = fetchStats(
            year = now.year,
            month = now.monthNumber,
            day = now.dayOfMonth,
            periodType = periodType,
        )
        // The chart's buckets depend on the period; entries are already in display order except the
        // day-of-month map which is keyed by number.
        // Buckets match the Android chart: hourly / day-of-week / week-of-month / month-of-year.
        val allBars = when (periodKey) {
            StatsPeriodKeys.DAY -> stats.hourly.entries.map { StatsBar(it.key, it.value) }
            StatsPeriodKeys.WEEK -> stats.weekly.entries.map { StatsBar(it.key, it.value) }
            StatsPeriodKeys.YEAR -> stats.yearly.entries.map { StatsBar(it.key, it.value) }
            else -> stats.monthly.entries.map { StatsBar(it.key, it.value) }
        }
        // Only show elapsed buckets — future hours/days/weeks/months would just drop the line to 0.
        val bars = when (periodKey) {
            StatsPeriodKeys.DAY -> allBars.filter {
                (it.label.substringBefore(":").toIntOrNull() ?: 0) <= now.hour
            }
            StatsPeriodKeys.WEEK -> allBars.take(now.dayOfWeek.isoDayNumber)
            StatsPeriodKeys.YEAR -> allBars.take(now.monthNumber)
            else -> allBars.take(((now.dayOfMonth - 1) / 7) + 1)
        }
        return StatsSnapshot(
            periodTotal = bars.sumOf { it.count },
            dailyAverage = stats.dailyAverage.toDouble(),
            bars = bars,
            triggers = stats.triggerBreakdown.map { StatsTrigger(label = it.label, count = it.count) },
        )
    }
}
