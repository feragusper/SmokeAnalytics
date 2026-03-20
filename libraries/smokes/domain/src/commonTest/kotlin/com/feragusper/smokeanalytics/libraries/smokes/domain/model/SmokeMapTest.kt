package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class SmokeMapTest {

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

    private fun smoke(id: String, latitude: Double, longitude: Double) = Smoke(
        id = id,
        date = Instant.fromEpochMilliseconds(0),
        location = GeoPoint(latitude, longitude),
    )
}
