package com.feragusper.smokeanalytics.features.home.domain

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationshipTrackingTest {

    @Test
    fun `cutoff is the feature launch date`() {
        assertEquals(Instant.parse("2026-06-15T00:00:00Z"), RelationshipTrackingSince)
    }

    @Test
    fun `smokes before the cutoff are excluded from pending`() {
        assertTrue(Instant.parse("2026-06-14T23:59:59Z") < RelationshipTrackingSince)
        assertTrue(Instant.parse("2026-06-15T00:00:00Z") >= RelationshipTrackingSince)
    }
}
