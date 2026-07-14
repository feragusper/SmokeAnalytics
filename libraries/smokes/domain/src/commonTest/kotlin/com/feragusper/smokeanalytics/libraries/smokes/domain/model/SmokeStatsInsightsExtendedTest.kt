package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SmokeStatsInsightsExtendedTest {

    private val tz = TimeZone.UTC

    @Test
    fun averageSummary_fullWeek_usesSevenDays() {
        val stats = SmokeStats(
            daily = emptyMap(),
            weekly = mapOf("Mon" to 2, "Tue" to 2, "Wed" to 2, "Thu" to 2, "Fri" to 2, "Sat" to 2, "Sun" to 2),
            monthly = emptyMap(),
            yearly = emptyMap(),
            hourly = emptyMap(),
            totalMonth = 0,
            totalWeek = 14,
            totalDay = 0,
            dailyAverage = 0f,
        )

        val summary = stats.averageSummary(
            period = SmokeStatsPeriod.WEEK,
            selectedDate = LocalDate(2023, 3, 6), // A past week
            now = Instant.parse("2023-03-20T12:00:00Z"),
            timeZone = tz,
        )

        assertEquals(StatsSummaryTitle.DailyPace, summary.title)
        assertEquals(StatsSummarySupporting.AcrossWeek, summary.supporting)
        assertEquals(2.0, summary.value)
    }

    @Test
    fun averageSummary_month_currentMonth_usesElapsedDays() {
        val stats = SmokeStats(
            daily = emptyMap(),
            weekly = emptyMap(),
            monthly = emptyMap(),
            yearly = emptyMap(),
            hourly = emptyMap(),
            totalMonth = 30,
            totalWeek = 0,
            totalDay = 0,
            dailyAverage = 0f,
        )

        val summary = stats.averageSummary(
            period = SmokeStatsPeriod.MONTH,
            selectedDate = LocalDate(2023, 3, 15),
            now = Instant.parse("2023-03-15T12:00:00Z"),
            timeZone = tz,
        )

        assertEquals(StatsSummaryTitle.DailyPace, summary.title)
        assertEquals(StatsSummarySupporting.AcrossElapsedMonth, summary.supporting)
        assertEquals(2.0, summary.value) // 30 / 15
    }

    @Test
    fun averageSummary_month_pastMonth_usesFullMonth() {
        val stats = SmokeStats(
            daily = emptyMap(),
            weekly = emptyMap(),
            monthly = emptyMap(),
            yearly = emptyMap(),
            hourly = emptyMap(),
            totalMonth = 31,
            totalWeek = 0,
            totalDay = 0,
            dailyAverage = 0f,
        )

        val summary = stats.averageSummary(
            period = SmokeStatsPeriod.MONTH,
            selectedDate = LocalDate(2023, 1, 15),
            now = Instant.parse("2023-03-15T12:00:00Z"),
            timeZone = tz,
        )

        assertEquals(StatsSummaryTitle.DailyPace, summary.title)
        assertEquals(StatsSummarySupporting.AcrossMonth, summary.supporting)
        assertEquals(1.0, summary.value) // 31 / 31
    }

    @Test
    fun averageSummary_year_currentYear_usesElapsedDays() {
        val stats = SmokeStats(
            daily = emptyMap(),
            weekly = emptyMap(),
            monthly = emptyMap(),
            yearly = mapOf("Jan" to 31, "Feb" to 28, "Mar" to 15),
            hourly = emptyMap(),
            totalMonth = 0,
            totalWeek = 0,
            totalDay = 0,
            dailyAverage = 0f,
        )

        val summary = stats.averageSummary(
            period = SmokeStatsPeriod.YEAR,
            selectedDate = LocalDate(2023, 3, 15),
            now = Instant.parse("2023-03-15T12:00:00Z"),
            timeZone = tz,
        )

        assertEquals(StatsSummaryTitle.DailyPace, summary.title)
        assertEquals(StatsSummarySupporting.AcrossElapsedYear, summary.supporting)
        val total = 31 + 28 + 15
        val elapsed = 74 // Jan 1 to Mar 15 = 74 days
        assertEquals(total.toDouble() / elapsed.toDouble(), summary.value, 0.01)
    }

    @Test
    fun averageSummary_year_pastYear_usesFullYear() {
        val stats = SmokeStats(
            daily = emptyMap(),
            weekly = emptyMap(),
            monthly = emptyMap(),
            yearly = mapOf("Jan" to 365),
            hourly = emptyMap(),
            totalMonth = 0,
            totalWeek = 0,
            totalDay = 0,
            dailyAverage = 0f,
        )

        val summary = stats.averageSummary(
            period = SmokeStatsPeriod.YEAR,
            selectedDate = LocalDate(2022, 6, 15),
            now = Instant.parse("2023-03-15T12:00:00Z"),
            timeZone = tz,
        )

        assertEquals(StatsSummaryTitle.DailyPace, summary.title)
        assertEquals(StatsSummarySupporting.AcrossYear, summary.supporting)
        assertEquals(1.0, summary.value) // 365 / 365
    }

    @Test
    fun averageSummary_day_pastDay_usesFullAwakeHours() {
        val stats = SmokeStats(
            daily = emptyMap(),
            weekly = emptyMap(),
            monthly = emptyMap(),
            yearly = emptyMap(),
            hourly = linkedMapOf(
                "08:00" to 1,
                "09:00" to 1,
                "10:00" to 1,
            ),
            totalMonth = 0,
            totalWeek = 0,
            totalDay = 3,
            dailyAverage = 0f,
        )

        val summary = stats.averageSummary(
            period = SmokeStatsPeriod.DAY,
            selectedDate = LocalDate(2023, 3, 1),
            now = Instant.parse("2023-03-15T12:00:00Z"), // Different day
            timeZone = tz,
        )

        assertEquals(StatsSummaryTitle.AwakeHourPace, summary.title)
        assertEquals(StatsSummarySupporting.AwakeHour, summary.supporting)
        assertEquals(1.0, summary.value) // 3 / 3
    }

    @Test
    fun averageSummary_overload_withYearMonthDay() {
        val stats = SmokeStats(
            daily = emptyMap(),
            weekly = emptyMap(),
            monthly = emptyMap(),
            yearly = emptyMap(),
            hourly = linkedMapOf("10:00" to 2),
            totalMonth = 0,
            totalWeek = 0,
            totalDay = 2,
            dailyAverage = 0f,
        )

        val summary = stats.averageSummary(
            period = SmokeStatsPeriod.DAY,
            selectedYear = 2023,
            selectedMonth = 3,
            selectedDay = 1,
            now = Instant.parse("2023-03-15T12:00:00Z"),
            timeZone = tz,
        )

        assertEquals(StatsSummaryTitle.AwakeHourPace, summary.title)
    }
}

