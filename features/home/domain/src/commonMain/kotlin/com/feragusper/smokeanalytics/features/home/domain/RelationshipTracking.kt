package com.feragusper.smokeanalytics.features.home.domain

import kotlinx.datetime.Instant

/**
 * Smokes logged before this date are not treated as "pending" — the reminder card doesn't nag
 * the user to tag their pre-feature history. The prompt shipped mid-June 2026; the cutoff sits
 * at July 1st so everything logged before the feature was actually in day-to-day use is exempt.
 */
val RelationshipTrackingSince: Instant = Instant.parse("2026-07-01T00:00:00Z")
