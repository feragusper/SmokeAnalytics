package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokeStatsUseCase
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** One bar of the daily chart: [label] is the day-of-month, [count] the cigarettes that day. */
data class StatsBar(val label: String, val count: Int)

/** A trigger/tag and how many cigarettes in the period carried it. */
data class StatsTrigger(val label: String, val count: Int)

/** Swift-friendly Analytics snapshot for the current month. */
data class StatsSnapshot(
    val totalMonth: Int,
    val totalWeek: Int,
    val dailyAverage: Double,
    val dailyBars: List<StatsBar>,
    val triggers: List<StatsTrigger>,
)

/** Swift-facing entry point for the Analytics screen. */
class StatsFacade : KoinComponent {

    private val fetchStats: FetchSmokeStatsUseCase by inject()

    @Throws(Throwable::class)
    suspend fun loadCurrentMonth(): StatsSnapshot {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val stats = fetchStats(
            year = now.year,
            month = now.monthNumber,
            day = now.dayOfMonth,
            periodType = FetchSmokeStatsUseCase.PeriodType.MONTH,
        )
        return StatsSnapshot(
            totalMonth = stats.totalMonth,
            totalWeek = stats.totalWeek,
            dailyAverage = stats.dailyAverage.toDouble(),
            dailyBars = stats.daily.entries
                .sortedBy { it.key.toIntOrNull() ?: 0 }
                .map { StatsBar(label = it.key, count = it.value) },
            triggers = stats.triggerBreakdown.map { StatsTrigger(label = it.label, count = it.count) },
        )
    }
}
