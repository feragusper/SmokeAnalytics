package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max

enum class SmokeStatsPeriod {
    DAY,
    WEEK,
    MONTH,
    YEAR,
}

data class SmokeStatsAverageSummary(
    val title: String,
    val supporting: String,
    val value: Double,
)

fun SmokeStats.averageSummary(
    period: SmokeStatsPeriod,
    selectedDate: LocalDate,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): SmokeStatsAverageSummary {
    val currentDate = now.toLocalDateTime(timeZone).date

    return when (period) {
        SmokeStatsPeriod.DAY -> {
            val totalAwakeHours = hourly.size.coerceAtLeast(1)
            val elapsedAwakeHours = if (selectedDate == currentDate) {
                currentElapsedAwakeHours(now, timeZone).coerceIn(1, totalAwakeHours)
            } else {
                totalAwakeHours
            }
            SmokeStatsAverageSummary(
                title = "Awake-hour pace",
                supporting = if (selectedDate == currentDate) {
                    "Average per awake hour so far"
                } else {
                    "Average per awake hour"
                },
                value = totalDay / elapsedAwakeHours.toDouble(),
            )
        }

        SmokeStatsPeriod.WEEK -> {
            val fullWeek = !selectedDate.isInSameWeekAs(currentDate)
            val elapsedDays = if (fullWeek) 7 else selectedDate.daysElapsedInWeek(currentDate)
            SmokeStatsAverageSummary(
                title = "Daily pace",
                supporting = if (fullWeek) {
                    "Across the selected week"
                } else {
                    "Across elapsed days in the selected week"
                },
                value = totalWeek / elapsedDays.toDouble(),
            )
        }

        SmokeStatsPeriod.MONTH -> {
            val daysInMonth = selectedDate.daysInMonth()
            val fullMonth = selectedDate.year != currentDate.year || selectedDate.monthNumber != currentDate.monthNumber
            val elapsedDays = if (fullMonth) daysInMonth else currentDate.dayOfMonth.coerceAtMost(daysInMonth)
            SmokeStatsAverageSummary(
                title = "Daily pace",
                supporting = if (fullMonth) {
                    "Across the selected month"
                } else {
                    "Across elapsed days in the selected month"
                },
                value = totalMonth / elapsedDays.toDouble(),
            )
        }

        SmokeStatsPeriod.YEAR -> {
            val daysInYear = selectedDate.daysInYear()
            val fullYear = selectedDate.year != currentDate.year
            val elapsedDays = if (fullYear) {
                daysInYear
            } else {
                currentDate.toEpochDays() - LocalDate(currentDate.year, 1, 1).toEpochDays() + 1
            }
            SmokeStatsAverageSummary(
                title = "Daily pace",
                supporting = if (fullYear) {
                    "Across the selected year"
                } else {
                    "Across elapsed days in the selected year"
                },
                value = yearly.values.sum() / elapsedDays.toDouble(),
            )
        }
    }
}

fun SmokeStats.averageSummary(
    period: SmokeStatsPeriod,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): SmokeStatsAverageSummary = averageSummary(
    period = period,
    selectedDate = LocalDate(selectedYear, selectedMonth, selectedDay),
    now = now,
    timeZone = timeZone,
)

private fun SmokeStats.currentElapsedAwakeHours(
    now: Instant,
    timeZone: TimeZone,
): Int {
    if (hourly.isEmpty()) return 1

    val labels = hourly.keys.toList()
    val currentHour = now.toLocalDateTime(timeZone).time.hour
    val currentLabel = "${currentHour.toString().padStart(2, '0')}:00"
    val exactIndex = labels.indexOf(currentLabel)
    if (exactIndex >= 0) return exactIndex + 1

    val hourValues = labels.map { label -> label.substringBefore(':').toInt() }
    val firstHour = hourValues.first()
    val lastHour = hourValues.last()
    val wrapsMidnight = firstHour > lastHour

    return when {
        wrapsMidnight && (currentHour >= firstHour || currentHour <= lastHour) -> labels.size
        !wrapsMidnight && currentHour > lastHour -> labels.size
        else -> 1
    }
}

private fun LocalDate.isInSameWeekAs(other: LocalDate): Boolean =
    startOfWeek().toEpochDays() == other.startOfWeek().toEpochDays()

private fun LocalDate.daysElapsedInWeek(currentDate: LocalDate): Int =
    max(1, (currentDate.toEpochDays() - startOfWeek().toEpochDays() + 1).toInt())

private fun LocalDate.startOfWeek(): LocalDate =
    plus(DatePeriod(days = -(dayOfWeek.isoDayNumber - 1)))

private fun LocalDate.daysInMonth(): Int =
    (LocalDate(year, monthNumber, 1).plus(DatePeriod(months = 1)).toEpochDays() - LocalDate(year, monthNumber, 1).toEpochDays()).toInt()

private fun LocalDate.daysInYear(): Int =
    (LocalDate(year + 1, 1, 1).toEpochDays() - LocalDate(year, 1, 1).toEpochDays()).toInt()
