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
 */
data class SmokeStats(
    val daily: Map<String, Int>,        // Día del mes -> Cantidad de cigarrillos
    val weekly: Map<String, Int>,       // Día de la semana (Mo, Tu, We) -> Cantidad de cigarrillos
    val monthly: Map<String, Int>,      // Semana del mes (Semana 1, 2, 3...) -> Cantidad de cigarrillos
    val yearly: Map<String, Int>,       // Mes del año (Jan, Feb, Mar) -> Cantidad de cigarrillos
    val hourly: Map<String, Int>,       // Hora del día ("00:00", "01:00" ...) -> Cantidad de cigarrillos
    val totalMonth: Int,                // Total del mes
    val totalWeek: Int,                 // Total de la semana
    val totalDay: Int,                  // Total del día
    val dailyAverage: Float              // Promedio diario
) {
    companion object {
        /**
         * Factory method to create a `SmokeStats` object from a list of smoke events.
         */
        fun from(smokes: List<Smoke>, year: Int, month: Int, day: Int?): SmokeStats {
            val yearMonth = YearMonth.of(year, month)
            val totalDaysInMonth = yearMonth.lengthOfMonth()
            val startOfMonth = LocalDate.of(year, month, 1)
            val endOfMonth = LocalDate.of(year, month, totalDaysInMonth)

            val filteredSmokes =
                smokes.filter { it.date.year == year && it.date.monthValue == month }

            // **Día del mes**
            val dailyStats = (1..totalDaysInMonth).associate { it.toString() to 0 }.toMutableMap()
            filteredSmokes.groupBy { it.date.dayOfMonth.toString() }
                .mapValues { it.value.size }
                .forEach { (day, count) -> dailyStats[day] = count }

            // **Día de la semana (Monday, Tuesday, ...)**
            val weeklyStats = DayOfWeek.entries
                .associate { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) to 0 }
                .toMutableMap()
            filteredSmokes.groupBy {
                it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
                .mapValues { it.value.size }
                .forEach { (day, count) -> weeklyStats[day] = count }

            // **Semana del mes (Semana 1, Semana 2, ...)**
            val weeksInMonth = (1..5).associate { "W$it" to 0 }.toMutableMap()
            filteredSmokes.groupBy { "W${((it.date.dayOfMonth - 1) / 7) + 1}" }
                .mapValues { it.value.size }
                .forEach { (week, count) -> weeksInMonth[week] = count }

            // **Mes del año (Jan, Feb, ...)**
            val yearlyStats = Month.entries
                .associate { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) to 0 }
                .toMutableMap()
            smokes.filter { it.date.year == year }
                .groupBy { it.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
                .mapValues { it.value.size }
                .forEach { (month, count) -> yearlyStats[month] = count }

            // **Consumo por hora en un día (00:00, 01:00, ..., 23:00)**
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
            val totalWeek =
                filteredSmokes.filter { it.date.toLocalDate() in startOfMonth..endOfMonth }.size
            val totalDay = dailyFilteredSmokes.size
            val dailyAverage =
                if (totalDaysInMonth > 0) totalMonth.toFloat() / totalDaysInMonth else 0f

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