package com.feragusper.smokeanalytics.libraries.cravings.data

import kotlinx.serialization.Serializable

/**
 * Firestore entity for a craving. See the mobile counterpart for the schema.
 */
@Serializable
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
