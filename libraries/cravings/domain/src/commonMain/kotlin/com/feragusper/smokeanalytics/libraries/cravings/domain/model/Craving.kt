package com.feragusper.smokeanalytics.libraries.cravings.domain.model

import kotlinx.datetime.Instant

/**
 * Represents a craving (the urge to smoke) that the user tracked instead of
 * immediately lighting a cigarette.
 *
 * The lifecycle is:
 *  1. The user feels the urge and taps "I feel like smoking". A [Craving] is
 *     created with [createdAt] = now and [outcome] = [CravingOutcome.PENDING].
 *  2. If the active goal says it is not time yet, the app suggests waiting and
 *     stores [targetAt] = the moment the user is allowed to smoke again.
 *  3. When the user comes back the craving is resolved: they either smoked after
 *     waiting ([CravingOutcome.POSTPONED]), let the urge pass entirely
 *     ([CravingOutcome.RESISTED]), or gave in without waiting
 *     ([CravingOutcome.GAVE_IN]). [resolvedAt] records when.
 *
 * @property id Stable identifier (Firestore document id).
 * @property createdAt When the urge was tracked.
 * @property targetAt When the user is allowed to smoke again, or null if no wait
 *   was required (already past the recommended gap, or no pacing goal active).
 * @property resolvedAt When the craving was resolved, or null while pending.
 * @property outcome How the craving ended.
 * @property pointsAwarded Reward points granted when the craving was resolved.
 */
data class Craving(
    val id: String,
    val createdAt: Instant,
    val targetAt: Instant? = null,
    val resolvedAt: Instant? = null,
    val outcome: CravingOutcome = CravingOutcome.PENDING,
    val pointsAwarded: Int = 0,
) {
    val isPending: Boolean get() = outcome == CravingOutcome.PENDING

    /** Whether this craving asked the user to wait (had a countdown). */
    val hadWait: Boolean get() = targetAt != null
}
