package com.feragusper.smokeanalytics.libraries.smokes.domain.model

import java.time.LocalDateTime

/**
 * Represents a single smoke event within the domain, encapsulating its unique identifier, the date and time
 * it occurred, and the time elapsed since the previous smoke event.
 *
 * @property id A unique identifier for the smoke event.
 * @property date The date and time when the smoke event occurred.
 * @property timeElapsedSincePreviousSmoke A pair representing the hours and minutes elapsed since the previous smoke event.
 */
data class Smoke(
    val id: String,
    val date: LocalDateTime,
    val timeElapsedSincePreviousSmoke: Pair<Long, Long>
)