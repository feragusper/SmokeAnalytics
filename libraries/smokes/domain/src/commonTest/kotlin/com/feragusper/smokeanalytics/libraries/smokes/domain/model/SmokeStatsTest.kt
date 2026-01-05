package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import kotlinx.datetime.Instant
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
}