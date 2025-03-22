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
        fun from(smokes: List<Smoke>, year: Int, month: Int, day: Int?): SmokeStats {
            val yearMonth = YearMonth.of(year, month)
            val totalDaysInMonth = yearMonth.lengthOfMonth()
            val startOfMonth = LocalDate.of(year, month, 1)
            val endOfMonth = LocalDate.of(year, month, totalDaysInMonth)

            val filteredSmokes =
                smokes.filter { it.date.year == year && it.date.monthValue == month }

            // Daily stats (1–31)
            val dailyStats = (1..totalDaysInMonth).associate { it.toString() to 0 }.toMutableMap()
            filteredSmokes.groupBy { it.date.dayOfMonth.toString() }
                .mapValues { it.value.size }
                .forEach { (day, count) -> dailyStats[day] = count }

            // Weekly stats (Mon–Sun)
            val weeklyStats = DayOfWeek.entries
                .associate { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) to 0 }
                .toMutableMap()
            filteredSmokes.groupBy {
                it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
                .mapValues { it.value.size }
                .forEach { (dayOfWeek, count) -> weeklyStats[dayOfWeek] = count }

            // Monthly stats (Week 1–5)
            val weekOfMonthStats = (1..5).associate { "W$it" to 0 }.toMutableMap()
            filteredSmokes.groupBy {
                val weekOfMonth = (it.date.dayOfMonth - 1) / 7 + 1
                "W$weekOfMonth"
            }
                .mapValues { it.value.size }
                .forEach { (week, count) -> weekOfMonthStats[week] = count }

            // Yearly stats (Jan–Dec)
            val yearlyStats = Month.entries
                .associate { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) to 0 }
                .toMutableMap()
            smokes.filter { it.date.year == year }
                .groupBy { it.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
                .mapValues { it.value.size }
                .forEach { (monthName, count) -> yearlyStats[monthName] = count }

            // Hourly stats (00:00–23:00)
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
            val totalWeek = filteredSmokes.count {
                it.date.toLocalDate() in startOfMonth..endOfMonth
            }
            val totalDay = dailyFilteredSmokes.size
            val dailyAverage = if (totalDaysInMonth > 0)
                totalMonth.toFloat() / totalDaysInMonth
            else 0f

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
    }

}
