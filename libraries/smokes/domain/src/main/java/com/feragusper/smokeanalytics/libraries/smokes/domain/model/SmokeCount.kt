package com.feragusper.smokeanalytics.libraries.smokes.domain.model

/**
 * Represents the count of smoke events categorized by time periods, including today, week, and month.
 * It also keeps track of the most recent smoke event.
 *
 * @property today A list of [Smoke] events that occurred today.
 * @property week The total count of smoke events for the current week.
 * @property month The total count of smoke events for the current month.
 * @property lastSmoke The most recent [Smoke] event, or null if no events have occurred.
 */
data class SmokeCount(
    val today: List<Smoke>,
    val week: Int,
    val month: Int,
    val lastSmoke: Smoke?,
)
