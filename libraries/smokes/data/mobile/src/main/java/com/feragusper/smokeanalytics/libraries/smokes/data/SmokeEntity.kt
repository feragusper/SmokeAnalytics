package com.feragusper.smokeanalytics.libraries.smokes.data

/**
 * Firestore entity for a smoke event.
 *
 * Schema (Android + Web):
 *  - timestampMillis: Double (epoch millis)
 */
data class SmokeEntity(
    val timestampMillis: Double = 0.0
) {
    object Fields {
        const val TIMESTAMP_MILLIS = "timestampMillis"
    }
}