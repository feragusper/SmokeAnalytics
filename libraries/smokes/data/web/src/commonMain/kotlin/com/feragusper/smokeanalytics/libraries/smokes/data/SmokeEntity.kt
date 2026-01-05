package com.feragusper.smokeanalytics.libraries.smokes.data

import kotlinx.serialization.Serializable

/**
 * Represents a smoke entity.
 *
 * @property timestampMillis The timestamp of the smoke.
 */
@Serializable
data class SmokeEntity(
    val timestampMillis: Double = 0.0
) {

    /**
     * Represents the fields of the smoke entity.
     */
    object Fields {
        const val TIMESTAMP_MILLIS = "timestampMillis"
    }
}