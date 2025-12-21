package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.periodUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class SmokeStats(
    val daily: Map<String, Int>,
    val weekly: Map<String, Int>,
    val monthly: Map<String, Int>,
    val yearly: Map<String, Int>,
    val hourly: Map<String, Int>,
    val totalMonth: Int,
    val totalWeek: Int,
    val totalDay: Int,
    val dailyAverage: Float
) {
    companion object {

        fun from(
            smokes: List<Smoke>,
            year: Int,
            month: Int, // 1..12
            day: Int?,  // 1..31 or null
            timeZone: TimeZone = TimeZone.currentSystemDefault(),
            now: Instant = Clock.System.now(),
        ): SmokeStats {
            val monthStart = LocalDate(year, month, 1)
            val nextMonthStart = monthStart.plus(DatePeriod(months = 1))
            val daysInMonth = monthStart.daysUntil(nextMonthStart)

            val monthStartInstant = monthStart.atStartOfDayIn(timeZone)
            val nextMonthStartInstant = nextMonthStart.atStartOfDayIn(timeZone)

            val monthSmokes = smokes
                .asSequence()
                .map { it to it.date.toLocalDateTime(timeZone) }
                .filter { (_, dt) -> dt.date.year == year && dt.date.monthNumber == month }
                .toList()

            // Daily: "1".."31"
            val dailyStats = (1..daysInMonth).associate { it.toString() to 0 }.toMutableMap()
            monthSmokes
                .groupBy { (_, dt) -> dt.date.dayOfMonth.toString() }
                .forEach { (k, v) -> dailyStats[k] = v.size }

            // Weekly: "Mon".."Sun" (fixed labels; locale lo hacés en UI si querés)
            val weeklyLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val weeklyStats = weeklyLabels.associateWith { 0 }.toMutableMap()
            monthSmokes
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
            val hourlyStats = (0..23).associate { hour ->
                hour.toTwoDigits() + ":00" to 0
            }.toMutableMap()

            val daySmokes = if (day != null) {
                monthSmokes.filter { (_, dt) -> dt.date.dayOfMonth == day }
            } else emptyList()

            daySmokes
                .groupBy { (_, dt) -> dt.time.hour.toTwoDigits() + ":00" }
                .forEach { (k, v) -> hourlyStats[k] = v.size }

            val totalMonth = monthSmokes.size

            // totalWeek: última semana “rolling” respecto a `now` (7 días hacia atrás)
            val totalWeek = run {
                val nowDateTime = now.toLocalDateTime(timeZone)
                val start = nowDateTime.date.plus(DatePeriod(days = -6)).atStartOfDayIn(timeZone)
                val end = nowDateTime.date.plus(DatePeriod(days = 1)).atStartOfDayIn(timeZone)
                smokes.count { it.date >= start && it.date < end }
            }

            val totalDay = daySmokes.size
            val dailyAverage = if (daysInMonth > 0) totalMonth.toFloat() / daysInMonth else 0f

            return SmokeStats(
                daily = dailyStats,
                weekly = weeklyStats,
                monthly = weekOfMonthStats,
                yearly = yearlyStats,
                hourly = hourlyStats,
                totalMonth = totalMonth,
                totalWeek = totalWeek,
                totalDay = totalDay,
                dailyAverage = dailyAverage
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
    }
}