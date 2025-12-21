package com.feragusper.smokeanalytics.libraries.smokes.data

import kotlinx.serialization.Serializable

/**
 * Firestore entity for a smoke event.
 *
 * Schema (Android + Web):
 *  - timestampMillis: Double (epoch millis)
 */
@Serializable
data class SmokeEntity(
    val timestampMillis: Double = 0.0
) {
    object Fields {
        const val TIMESTAMP_MILLIS = "timestampMillis"
    }
}