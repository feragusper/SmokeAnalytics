package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Represents statistical data for smoke events over different time frames (daily, weekly, monthly, and yearly).
 *
 * @property daily Number of cigarettes smoked per day of the month.
 * @property weekly Number of cigarettes smoked per day of the week (e.g., Mon, Tue, Wed).
 * @property monthly Number of cigarettes smoked per week of the month (Week 1, Week 2, ...).
 * @property yearly Number of cigarettes smoked per month of the year (Jan, Feb, ...).
 * @property hourly Number of cigarettes smoked per hour of the day ("00:00", "01:00", ...).
 * @property totalMonth Total number of cigarettes smoked in the month.
 * @property totalWeek Total number of cigarettes smoked in the current week.
 * @property totalDay Total number of cigarettes smoked today.
 * @property dailyAverage Average number of cigarettes smoked per day in the month.
 */
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
        /**
         * Factory method to create a `SmokeStats` object from a list of smoke events.
         *
         * @param smokes The list of [Smoke] events to calculate statistics for.
         * @param year The year to calculate statistics for.
         * @param month The month to calculate statistics for.
         * @param day (Optional) The specific day to calculate hourly statistics for.
         * @return A [SmokeStats] object containing the calculated statistics.
         */
        fun from(smokes: List<Smoke>, year: Int, month: Int, day: Int?): SmokeStats {
            val yearMonth = YearMonth.of(year, month)
            val totalDaysInMonth = yearMonth.lengthOfMonth()
            val startOfMonth = LocalDate.of(year, month, 1)
            val endOfMonth = LocalDate.of(year, month, totalDaysInMonth)

            val filteredSmokes =
                smokes.filter { it.date.year == year && it.date.monthValue == month }

            // Daily Statistics
            val dailyStats = (1..totalDaysInMonth).associate { it.toString() to 0 }.toMutableMap()
            filteredSmokes.groupBy { it.date.dayOfMonth.toString() }
                .mapValues { it.value.size }
                .forEach { (day, count) -> dailyStats[day] = count }

            // Weekly Statistics
            val weeklyStats = DayOfWeek.entries
                .associate { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) to 0 }
                .toMutableMap()
            filteredSmokes.groupBy {
                it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
                .mapValues { it.value.size }
                .forEach { (day, count) -> weeklyStats[day] = count }

            // Monthly Statistics (Week of the Month)
            val weeksInMonth = (1..5).associate { "W$it" to 0 }.toMutableMap()
            filteredSmokes.groupBy { "W${((it.date.dayOfMonth - 1) / 7) + 1}" }
                .mapValues { it.value.size }
                .forEach { (week, count) -> weeksInMonth[week] = count }

            // Yearly Statistics (Month of the Year)
            val yearlyStats = Month.entries
                .associate { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) to 0 }
                .toMutableMap()
            smokes.filter { it.date.year == year }
                .groupBy { it.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
                .mapValues { it.value.size }
                .forEach { (month, count) -> yearlyStats[month] = count }

            // Hourly Statistics (Time of the Day)
            val hourlyStats = (0..23).associate {
                LocalTime.of(it, 0).format(DateTimeFormatter.ofPattern("HH:00")) to 0
            }.toMutableMap()

            val dailyFilteredSmokes = if (day != null) {
                filteredSmokes.filter { it.date.dayOfMonth == day }
            } else emptyList()

            dailyFilteredSmokes.groupBy {
                it.date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:00"))
            }
                .mapValues { it.value.size }
                .forEach { (hour, count) -> hourlyStats[hour] = count }

            val totalMonth = filteredSmokes.size
            val totalWeek = filteredSmokes.filter {
                it.date.toLocalDate() in startOfMonth..endOfMonth
            }.size
            val totalDay = dailyFilteredSmokes.size
            val dailyAverage = if (totalDaysInMonth > 0)
                totalMonth.toFloat() / totalDaysInMonth
            else 0f

            return SmokeStats(
                dailyStats,
                weeklyStats,
                weeksInMonth,
                yearlyStats,
                hourlyStats,
                totalMonth,
                totalWeek,
                totalDay,
                dailyAverage
            )
        }
    }
}
