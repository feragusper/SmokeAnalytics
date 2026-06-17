package com.feragusper.smokeanalytics.libraries.cravings.domain.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class CravingStatsTest {

    private val base = Instant.parse("2026-06-16T12:00:00Z")

    private fun craving(
        id: String,
        outcome: CravingOutcome,
        waited: Int,
        points: Int,
    ) = Craving(
        id = id,
        createdAt = base,
        targetAt = base + waited.minutes,
        resolvedAt = base + waited.minutes,
        outcome = outcome,
        pointsAwarded = points,
    )

    @Test
    fun `empty list yields zeroed stats`() {
        val stats = emptyList<Craving>().toCravingStats()
        assertEquals(CravingStats(), stats)
        assertEquals(0f, stats.winRate)
    }

    @Test
    fun `pending cravings are ignored`() {
        val stats = listOf(
            Craving(id = "1", createdAt = base, outcome = CravingOutcome.PENDING),
        ).toCravingStats()
        assertEquals(0, stats.total)
    }

    @Test
    fun `aggregates outcomes minutes and points`() {
        val stats = listOf(
            craving("1", CravingOutcome.RESISTED, waited = 40, points = 18),
            craving("2", CravingOutcome.POSTPONED, waited = 20, points = 4),
            craving("3", CravingOutcome.GAVE_IN, waited = 0, points = 0),
        ).toCravingStats()

        assertEquals(3, stats.total)
        assertEquals(1, stats.resisted)
        assertEquals(1, stats.postponed)
        assertEquals(1, stats.gaveIn)
        assertEquals(60, stats.minutesWaited)
        assertEquals(22, stats.points)
        assertEquals(2f / 3f, stats.winRate)
    }
}
