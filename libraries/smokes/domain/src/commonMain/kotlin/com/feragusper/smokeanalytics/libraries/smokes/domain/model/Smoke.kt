package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import kotlinx.datetime.Instant

/**
 * Represents a smoke.
 *
 * @property id The id of the smoke.
 * @property date The date of the smoke.
 * @property timeElapsedSincePreviousSmoke The time elapsed since the previous smoke.
 * @property location Where the smoke was logged, if location tracking was on.
 * @property relationship What the smoke was related to (coffee, boredom, ...). Defaults
 *   to [SmokeRelationship.Untracked] so existing/watch smokes keep working unchanged.
 */
data class Smoke(
    val id: String,
    val date: Instant,
    val timeElapsedSincePreviousSmoke: Pair<Long, Long> = 0L to 0L,
    val location: GeoPoint? = null,
    val relationship: SmokeRelationship = SmokeRelationship.Untracked,
)
