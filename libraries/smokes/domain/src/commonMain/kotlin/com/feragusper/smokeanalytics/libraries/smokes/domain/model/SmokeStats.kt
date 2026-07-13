package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.periodUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class SmokeStats(
    val daily: Map<String, Int>,
    val weekly: Map<String, Int>,
    val monthly: Map<String, Int>,
    val yearly: Map<String, Int>,
    val hourly: Map<String, Int>,
    val totalMonth: Int,
    val totalWeek: Int,
    val totalDay: Int,
    val dailyAverage: Float,
    /** Tag → count over the selected period, ranked by count desc. */
    val triggerBreakdown: List<TriggerCount> = emptyList(),
) {
    /** A trigger and how many cigarettes in the period were tagged with it. */
    data class TriggerCount(
        val key: String,
        val label: String,
        val count: Int,
    )

    enum class SelectionPeriod {
        DAY,
        WEEK,
        MONTH,
        YEAR,
    }

    companion object {

        fun from(
            smokes: List<Smoke>,
            year: Int,
            month: Int, // 1..12
            day: Int?,  // 1..31 or null
            timeZone: TimeZone = TimeZone.currentSystemDefault(),
            now: Instant = Clock.System.now(),
            dayStartHour: Int = 0,
            bedtimeHour: Int = 22,
            periodType: SelectionPeriod = SelectionPeriod.MONTH,
            triggerLabelOverrides: Map<String, String> = emptyMap(),
            weekStartsMonday: Boolean = true,
        ): SmokeStats {
            val monthStart = LocalDate(year, month, 1)
            val nextMonthStart = monthStart.plus(DatePeriod(months = 1))
            val daysInMonth = monthStart.daysUntil(nextMonthStart)

            val shiftedSmokes = smokes
                .asSequence()
                .map { smoke -> smoke to smoke.date.minus(dayStartHour, DateTimeUnit.HOUR, timeZone).toLocalDateTime(timeZone) }
                .toList()

            val monthSmokes = shiftedSmokes
                .asSequence()
                .filter { (_, dt) -> dt.date.year == year && dt.date.monthNumber == month }
                .toList()

            // Daily: "1".."31"
            val dailyStats = (1..daysInMonth).associate { it.toString() to 0 }.toMutableMap()
            monthSmokes
                .groupBy { (_, dt) -> dt.date.dayOfMonth.toString() }
                .forEach { (k, v) -> dailyStats[k] = v.size }

            // Weekly: fixed English day keys; ordered per the user's week-start preference so
            // the chart begins on Monday or Sunday. Order matters — weeklyStats is a LinkedHashMap.
            val weeklyLabels = if (weekStartsMonday) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            } else {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            }
            val weeklyStats = weeklyLabels.associateWith { 0 }.toMutableMap()
            val weekSource = if (periodType == SelectionPeriod.WEEK) shiftedSmokes else monthSmokes
            weekSource
                .groupBy { (_, dt) -> dt.dayOfWeek.toShortLabel() }
                .forEach { (k, v) -> weeklyStats[k] = v.size }

            // Monthly: "W1".."W5"
            val weekOfMonthStats = (1..5).associate { "W$it" to 0 }.toMutableMap()
            monthSmokes
                .groupBy { (_, dt) ->
                    val weekOfMonth = ((dt.date.dayOfMonth - 1) / 7) + 1
                    "W$weekOfMonth"
                }
                .forEach { (k, v) -> weekOfMonthStats[k] = v.size }

            // Yearly: "Jan".."Dec" (fixed labels; locale en UI)
            val yearlyLabels = listOf(
                "Jan",
                "Feb",
                "Mar",
                "Apr",
                "May",
                "Jun",
                "Jul",
                "Aug",
                "Sep",
                "Oct",
                "Nov",
                "Dec"
            )
            val yearlyStats = yearlyLabels.associateWith { 0 }.toMutableMap()
            smokes
                .asSequence()
                .map { it.date.toLocalDateTime(timeZone).date }
                .filter { it.year == year }
                .groupBy { it.monthNumber }
                .forEach { (monthNumber, list) ->
                    yearlyStats[yearlyLabels[monthNumber - 1]] = list.size
                }

            // Hourly: "00:00".."23:00"
            val hourlyLabels = awakeHoursSequence(
                dayStartHour = dayStartHour,
                bedtimeHour = bedtimeHour,
            )
                .map { hour -> "${hour.toTwoDigits()}:00" }
            val hourlyStats = hourlyLabels.associateWith { 0 }.toMutableMap()

            val daySmokes = if (day != null) {
                monthSmokes.filter { (_, dt) -> dt.date.dayOfMonth == day }
            } else emptyList()

            daySmokes
                .groupBy { (smoke, _) -> smoke.date.toLocalDateTime(timeZone).time.hour.toTwoDigits() + ":00" }
                .forEach { (k, v) ->
                    if (hourlyStats.containsKey(k)) {
                        hourlyStats[k] = v.size
                    }
                }

            val totalMonth = monthSmokes.size

            val totalWeek = run {
                if (periodType == SelectionPeriod.WEEK) {
                    smokes.size
                } else {
                    val shiftedNow = now.minus(dayStartHour, DateTimeUnit.HOUR, timeZone).toLocalDateTime(timeZone)
                    val start = shiftedNow.date.plus(DatePeriod(days = -6))
                        .atStartOfDayIn(timeZone)
                        .plus(dayStartHour, DateTimeUnit.HOUR, timeZone)
                    val end = shiftedNow.date.plus(DatePeriod(days = 1))
                        .atStartOfDayIn(timeZone)
                        .plus(dayStartHour, DateTimeUnit.HOUR, timeZone)
                    smokes.count { it.date >= start && it.date < end }
                }
            }

            val totalDay = daySmokes.size
            val dailyAverage = if (daysInMonth > 0) totalMonth.toFloat() / daysInMonth else 0f

            // Trigger breakdown over the smokes in the requested range (the use case fetches
            // exactly the selected period), ranked by frequency.
            val tagCounts = mutableMapOf<String, Int>()
            smokes.forEach { smoke ->
                (smoke.relationship as? SmokeRelationship.Tagged)?.tags?.forEach { tag ->
                    tagCounts[tag] = (tagCounts[tag] ?: 0) + 1
                }
            }
            val triggerBreakdown = tagCounts.entries
                .sortedByDescending { it.value }
                .map {
                    TriggerCount(
                        key = it.key,
                        label = triggerLabelOverrides[it.key]?.trim()?.takeIf(String::isNotEmpty)
                            ?: SmokeTrigger.labelFor(it.key),
                        count = it.value,
                    )
                }

            return SmokeStats(
                daily = dailyStats,
                weekly = weeklyStats,
                monthly = weekOfMonthStats,
                yearly = yearlyStats,
                hourly = hourlyStats,
                totalMonth = totalMonth,
                totalWeek = totalWeek,
                totalDay = totalDay,
                dailyAverage = dailyAverage,
                triggerBreakdown = triggerBreakdown,
            )
        }

        private fun LocalDate.daysUntil(other: LocalDate): Int =
            this.periodUntil(other).days

        private fun DayOfWeek.toShortLabel(): String = when (this) {
            DayOfWeek.MONDAY -> "Mon"
            DayOfWeek.TUESDAY -> "Tue"
            DayOfWeek.WEDNESDAY -> "Wed"
            DayOfWeek.THURSDAY -> "Thu"
            DayOfWeek.FRIDAY -> "Fri"
            DayOfWeek.SATURDAY -> "Sat"
            DayOfWeek.SUNDAY -> "Sun"
            else -> ""
        }

        private fun Int.toTwoDigits(): String = if (this < 10) "0$this" else this.toString()

        private fun awakeHoursSequence(
            dayStartHour: Int,
            bedtimeHour: Int,
        ): List<Int> {
            val normalizedStart = dayStartHour.mod(24)
            val normalizedEnd = bedtimeHour.mod(24)
            val duration = (normalizedEnd - normalizedStart).mod(24).takeIf { it > 0 } ?: 16
            return (0 until duration).map { offset -> (normalizedStart + offset).mod(24) }
        }
    }
}
