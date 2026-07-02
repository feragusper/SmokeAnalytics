package com.feragusper.smokeanalytics.features.home.domain

import kotlinx.datetime.Instant

/**
 * The relationship (trigger) prompt shipped mid-June 2026. Smokes logged before this date were
 * never offered the prompt, so they are not treated as "pending" — the reminder card doesn't nag
 * the user to tag their entire pre-feature history.
 */
val RelationshipTrackingSince: Instant = Instant.parse("2026-06-15T00:00:00Z")
