package com.feragusper.smokeanalytics.libraries.architecture.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Advanceable clock so the gate's TTL window can be exercised deterministically. */
private class FakeClock(var instant: Instant) : Clock {
    override fun now(): Instant = instant
}

class ReadFreshnessGateTest {

    private val start = Instant.fromEpochMilliseconds(1_000_000L)

    @Test
    fun `forceRefresh always resolves to server`() {
        val gate = ReadFreshnessGate(ttl = 4.hours, clock = FakeClock(start))
        assertEquals(DataSource.SERVER, gate.resolveSource(forceRefresh = true))
    }

    @Test
    fun `first background load with no prior server sync resolves to server`() {
        val gate = ReadFreshnessGate(ttl = 4.hours, clock = FakeClock(start))
        assertEquals(DataSource.SERVER, gate.resolveSource(forceRefresh = false))
    }

    @Test
    fun `within ttl after a server load resolves to cache`() {
        val clock = FakeClock(start)
        val gate = ReadFreshnessGate(ttl = 4.hours, clock = clock)
        gate.markServerLoad()
        clock.instant = start + 3.hours
        assertEquals(DataSource.CACHE, gate.resolveSource(forceRefresh = false))
    }

    @Test
    fun `at or past ttl after a server load resolves to server again`() {
        val clock = FakeClock(start)
        val gate = ReadFreshnessGate(ttl = 4.hours, clock = clock)
        gate.markServerLoad()
        clock.instant = start + 4.hours + 1.minutes
        assertEquals(DataSource.SERVER, gate.resolveSource(forceRefresh = false))
    }

    @Test
    fun `markServerLoad restarts the freshness window`() {
        val clock = FakeClock(start)
        val gate = ReadFreshnessGate(ttl = 4.hours, clock = clock)
        gate.markServerLoad()
        // Past the first window → would be SERVER…
        clock.instant = start + 5.hours
        assertEquals(DataSource.SERVER, gate.resolveSource(forceRefresh = false))
        // …but a fresh server load restarts it, so the next nearby read is served from cache.
        gate.markServerLoad()
        clock.instant = clock.instant + 1.hours
        assertEquals(DataSource.CACHE, gate.resolveSource(forceRefresh = false))
    }
}
