package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class SmokeStatsTest {

    private val tz = TimeZone.UTC

    private fun createSmokeEvents(): List<Smoke> {
        return listOf(
            Smoke("1", Instant.parse("2023-03-01T12:00:00Z"), 0L to 0L), // Wed
            Smoke("2", Instant.parse("2023-03-02T13:00:00Z"), 0L to 0L), // Thu
            Smoke("3", Instant.parse("2023-03-08T14:30:00Z"), 1L to 30L), // Wed
            Smoke("4", Instant.parse("2023-03-15T16:15:00Z"), 2L to 0L),  // Wed
            Smoke("5", Instant.parse("2023-03-22T10:00:00Z"), 2L to 30L), // Wed
            Smoke("6", Instant.parse("2023-03-29T11:00:00Z"), 3L to 0L),  // Wed
        )
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct weekly statistics`() {
        val smokes = createSmokeEvents()

        val stats = SmokeStats.from(
            smokes = smokes,
            year = 2023,
            month = 3,
            day = null,
            timeZone = tz,
            now = Instant.parse("2023-03-15T00:00:00Z")
        )

        assertEquals(5, stats.weekly["Wed"])
        assertEquals(1, stats.weekly["Thu"])
        assertEquals(0, stats.weekly["Mon"])
        assertEquals(0, stats.weekly["Tue"])
        assertEquals(0, stats.weekly["Fri"])
        assertEquals(0, stats.weekly["Sat"])
        assertEquals(0, stats.weekly["Sun"])
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct monthly statistics`() {
        val smokes = createSmokeEvents()

        val stats = SmokeStats.from(
            smokes = smokes,
            year = 2023,
            month = 3,
            day = null,
            timeZone = tz,
            now = Instant.parse("2023-03-15T00:00:00Z")
        )

        assertEquals(2, stats.monthly["W1"]) // days 1..7
        assertEquals(1, stats.monthly["W2"]) // days 8..14
        assertEquals(1, stats.monthly["W3"]) // days 15..21
        assertEquals(1, stats.monthly["W4"]) // days 22..28
        assertEquals(1, stats.monthly["W5"]) // days 29..31
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct yearly statistics`() {
        val smokes = createSmokeEvents()

        val stats = SmokeStats.from(
            smokes = smokes,
            year = 2023,
            month = 3,
            day = null,
            timeZone = tz,
            now = Instant.parse("2023-03-15T00:00:00Z")
        )

        assertEquals(0, stats.yearly["Jan"])
        assertEquals(0, stats.yearly["Feb"])
        assertEquals(6, stats.yearly["Mar"])
        assertEquals(0, stats.yearly["Apr"])
        assertEquals(0, stats.yearly["May"])
        assertEquals(0, stats.yearly["Jun"])
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct hourly statistics for a given day`() {
        val smokes = createSmokeEvents()

        val stats = SmokeStats.from(
            smokes = smokes,
            year = 2023,
            month = 3,
            day = 1,
            timeZone = tz,
            now = Instant.parse("2023-03-15T00:00:00Z")
        )

        assertEquals(1, stats.hourly["12:00"])
        assertEquals(0, stats.hourly["13:00"])
        assertEquals(0, stats.hourly["14:00"])
    }

    @Test
    fun `GIVEN smoke events WHEN from is called THEN it should return correct total month statistics`() {
        val smokes = createSmokeEvents()

        val stats = SmokeStats.from(
            smokes = smokes,
            year = 2023,
            month = 3,
            day = null,
            timeZone = tz,
            now = Instant.parse("2023-03-15T00:00:00Z")
        )

        assertEquals(6, stats.totalMonth)
    }

    @Test
    fun `GIVEN now within last 7 days window WHEN from is called THEN it should compute rolling totalWeek`() {
        val smokes = createSmokeEvents()

        // Rolling window: start = 2023-03-16T00:00Z (now date 22 minus 6 days), end = 2023-03-23T00:00Z
        // In that range we only have: 2023-03-22T10:00Z (id "5") => 1 smoke
        val stats = SmokeStats.from(
            smokes = smokes,
            year = 2023,
            month = 3,
            day = null,
            timeZone = tz,
            now = Instant.parse("2023-03-22T08:00:00Z")
        )

        assertEquals(1, stats.totalWeek)
    }

    @Test
    fun `GIVEN a week crossing month boundary WHEN from is called for week THEN weekday buckets include prior-month days`() {
        val smokes = listOf(
            Smoke("1", Instant.parse("2023-03-31T10:00:00Z"), 0L to 0L), // Fri
            Smoke("2", Instant.parse("2023-04-01T11:00:00Z"), 0L to 0L), // Sat
            Smoke("3", Instant.parse("2023-04-02T12:00:00Z"), 0L to 0L), // Sun
        )

        val stats = SmokeStats.from(
            smokes = smokes,
            year = 2023,
            month = 4,
            day = 1,
            timeZone = tz,
            now = Instant.parse("2023-04-02T12:00:00Z"),
            periodType = SmokeStats.SelectionPeriod.WEEK,
        )

        assertEquals(1, stats.weekly["Fri"])
        assertEquals(1, stats.weekly["Sat"])
        assertEquals(1, stats.weekly["Sun"])
        assertEquals(3, stats.totalWeek)
    }

    @Test
    fun `GIVEN day is provided WHEN from is called THEN totalDay should match daySmokes`() {
        val smokes = createSmokeEvents()

        val stats = SmokeStats.from(
            smokes = smokes,
            year = 2023,
            month = 3,
            day = 2,
            timeZone = tz,
            now = Instant.parse("2023-03-15T00:00:00Z")
        )

        assertEquals(1, stats.totalDay)
    }

    @Test
    fun `GIVEN bedtime is configured WHEN from is called for day THEN hourly stats exclude sleep hours`() {
        val smokes = listOf(
            Smoke("1", Instant.parse("2023-03-01T07:00:00Z"), 0L to 0L),
            Smoke("2", Instant.parse("2023-03-01T21:00:00Z"), 0L to 0L),
            Smoke("3", Instant.parse("2023-03-01T23:00:00Z"), 0L to 0L),
        )

        val stats = SmokeStats.from(
            smokes = smokes,
            year = 2023,
            month = 3,
            day = 1,
            timeZone = tz,
            now = Instant.parse("2023-03-01T23:30:00Z"),
            dayStartHour = 6,
            bedtimeHour = 22,
        )

        assertEquals(16, stats.hourly.size)
        assertEquals(1, stats.hourly["07:00"])
        assertEquals(1, stats.hourly["21:00"])
        assertEquals(null, stats.hourly["23:00"])
    }

    @Test
    fun `GIVEN current week stats WHEN averageSummary is called THEN it uses elapsed days in week`() {
        val stats = SmokeStats(
            daily = emptyMap(),
            weekly = mapOf("Mon" to 10, "Tue" to 8, "Wed" to 6, "Thu" to 4, "Fri" to 0, "Sat" to 0, "Sun" to 0),
            monthly = emptyMap(),
            yearly = emptyMap(),
            hourly = emptyMap(),
            totalMonth = 0,
            totalWeek = 28,
            totalDay = 0,
            dailyAverage = 0f,
        )

        val summary = stats.averageSummary(
            period = SmokeStatsPeriod.WEEK,
            selectedDate = LocalDate(2023, 3, 16),
            now = Instant.parse("2023-03-16T12:00:00Z"),
            timeZone = tz,
        )

        assertEquals("Daily pace", summary.title)
        assertEquals("Across elapsed days in the selected week", summary.supporting)
        assertEquals(7.0, summary.value)
    }

    @Test
    fun `GIVEN current day stats WHEN averageSummary is called THEN it uses elapsed awake hours`() {
        val stats = SmokeStats(
            daily = emptyMap(),
            weekly = emptyMap(),
            monthly = emptyMap(),
            yearly = emptyMap(),
            hourly = linkedMapOf(
                "08:00" to 1,
                "09:00" to 0,
                "10:00" to 1,
                "11:00" to 0,
                "12:00" to 1,
                "13:00" to 0,
            ),
            totalMonth = 0,
            totalWeek = 0,
            totalDay = 3,
            dailyAverage = 0f,
        )

        val summary = stats.averageSummary(
            period = SmokeStatsPeriod.DAY,
            selectedDate = LocalDate(2023, 3, 1),
            now = Instant.parse("2023-03-01T12:30:00Z"),
            timeZone = tz,
        )

        assertEquals("Awake-hour pace", summary.title)
        assertEquals("Average per awake hour so far", summary.supporting)
        assertEquals(0.6, summary.value)
    }
}
