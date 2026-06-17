package com.feragusper.smokeanalytics.libraries.cravings.data

/**
 * Firestore entity for a craving.
 *
 * Schema (Android + Web):
 *  - createdAtMillis: Double (epoch millis)
 *  - targetAtMillis: Double? (epoch millis, null when no wait was required)
 *  - resolvedAtMillis: Double? (epoch millis, null while pending)
 *  - outcome: String (CravingOutcome name)
 *  - pointsAwarded: Double (stored as number)
 */
data class CravingEntity(
    val createdAtMillis: Double = 0.0,
    val targetAtMillis: Double? = null,
    val resolvedAtMillis: Double? = null,
    val outcome: String = "PENDING",
    val pointsAwarded: Double = 0.0,
) {
    object Fields {
        const val CREATED_AT_MILLIS = "createdAtMillis"
        const val TARGET_AT_MILLIS = "targetAtMillis"
        const val RESOLVED_AT_MILLIS = "resolvedAtMillis"
        const val OUTCOME = "outcome"
        const val POINTS_AWARDED = "pointsAwarded"
    }
}
