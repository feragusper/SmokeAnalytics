package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SmokeMapTest {

    private val utc = TimeZone.UTC

    @Test
    fun `clusterSmokesForMap groups nearby points more tightly for day period`() {
        val smokes = listOf(
            smoke("1", 40.4168, -3.7038),
            smoke("2", 40.41681, -3.70381),
            smoke("3", 40.4189, -3.7090),
        )

        val clusters = clusterSmokesForMap(smokes, SmokeMapPeriod.Day)

        assertEquals(2, clusters.size)
        assertEquals(2, clusters.first().count)
        assertEquals(120, clusters.first().radiusMeters)
    }

    @Test
    fun `clusterSmokesForMap uses broader grouping for month period`() {
        val smokes = listOf(
            smoke("1", 40.4168, -3.7038),
            smoke("2", 40.4174, -3.7041),
        )

        val clusters = clusterSmokesForMap(smokes, SmokeMapPeriod.Month)

        assertEquals(1, clusters.size)
        assertEquals(2, clusters.first().count)
        assertEquals(900, clusters.first().radiusMeters)
    }

    @Test
    fun `clusterSmokesForMap uses year period radius of 1200`() {
        val smokes = listOf(
            smoke("1", 40.4168, -3.7038),
            smoke("2", 40.4174, -3.7041),
        )

        val clusters = clusterSmokesForMap(smokes, SmokeMapPeriod.Year)

        assertEquals(1, clusters.size)
        assertEquals(1200, clusters.first().radiusMeters)
    }

    @Test
    fun `smokeMapRange with selectedDate day returns 24h window starting at dayStartHour`() {
        val date = LocalDate(2026, 3, 15)
        val (start, end) = smokeMapRange(
            period = SmokeMapPeriod.Day,
            timeZone = utc,
            dayStartHour = 6,
            selectedDate = date,
        )

        assertEquals(Instant.parse("2026-03-15T06:00:00Z"), start)
        assertEquals(Instant.parse("2026-03-16T06:00:00Z"), end)
    }

    @Test
    fun `smokeMapRange with selectedDate day and zero dayStartHour starts at midnight`() {
        val date = LocalDate(2026, 3, 15)
        val (start, end) = smokeMapRange(
            period = SmokeMapPeriod.Day,
            timeZone = utc,
            dayStartHour = 0,
            selectedDate = date,
        )

        assertEquals(Instant.parse("2026-03-15T00:00:00Z"), start)
        assertEquals(Instant.parse("2026-03-16T00:00:00Z"), end)
    }

    @Test
    fun `smokeMapRange with selectedDate week returns Monday-based 7-day window`() {
        // 2026-03-18 is Wednesday (ISO day 3), so week starts Monday 2026-03-16
        val date = LocalDate(2026, 3, 18)
        val (start, end) = smokeMapRange(
            period = SmokeMapPeriod.Week,
            timeZone = utc,
            dayStartHour = 0,
            selectedDate = date,
        )

        assertEquals(Instant.parse("2026-03-16T00:00:00Z"), start)
        assertEquals(Instant.parse("2026-03-23T00:00:00Z"), end)
    }

    @Test
    fun `smokeMapRange with selectedDate month returns full calendar month`() {
        val date = LocalDate(2026, 3, 15)
        val (start, end) = smokeMapRange(
            period = SmokeMapPeriod.Month,
            timeZone = utc,
            dayStartHour = 0,
            selectedDate = date,
        )

        assertEquals(Instant.parse("2026-03-01T00:00:00Z"), start)
        assertEquals(Instant.parse("2026-04-01T00:00:00Z"), end)
    }

    @Test
    fun `smokeMapRange with selectedDate year returns full calendar year`() {
        val date = LocalDate(2026, 3, 15)
        val (start, end) = smokeMapRange(
            period = SmokeMapPeriod.Year,
            timeZone = utc,
            dayStartHour = 0,
            selectedDate = date,
        )

        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), start)
        assertEquals(Instant.parse("2027-01-01T00:00:00Z"), end)
    }

    @Test
    fun `smokeMapRange with selectedDate year respects dayStartHour`() {
        val date = LocalDate(2026, 6, 1)
        val (start, end) = smokeMapRange(
            period = SmokeMapPeriod.Year,
            timeZone = utc,
            dayStartHour = 6,
            selectedDate = date,
        )

        assertEquals(Instant.parse("2026-01-01T06:00:00Z"), start)
        assertEquals(Instant.parse("2027-01-01T06:00:00Z"), end)
    }

    @Test
    fun `smokeMapRange year without selectedDate spans current year`() {
        val now = Instant.parse("2026-06-03T12:00:00Z")
        val (start, end) = smokeMapRange(
            period = SmokeMapPeriod.Year,
            timeZone = utc,
            dayStartHour = 0,
            now = now,
        )

        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), start)
        assertEquals(Instant.parse("2027-01-01T00:00:00Z"), end)
    }

    @Test
    fun `smokeMapRange with null selectedDate uses current time`() {
        val now = Instant.parse("2026-06-03T12:00:00Z")
        val (start, end) = smokeMapRange(
            period = SmokeMapPeriod.Day,
            timeZone = utc,
            dayStartHour = 0,
            now = now,
            selectedDate = null,
        )

        assertTrue(start <= now)
        assertTrue(end > now)
    }

    private fun smoke(id: String, latitude: Double, longitude: Double) = Smoke(
        id = id,
        date = Instant.fromEpochMilliseconds(0),
        location = GeoPoint(latitude, longitude),
    )
}
