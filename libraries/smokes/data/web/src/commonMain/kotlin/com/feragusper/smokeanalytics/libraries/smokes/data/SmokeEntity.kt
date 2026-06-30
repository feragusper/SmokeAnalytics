package com.feragusper.smokeanalytics.libraries.smokes.data

import kotlinx.serialization.Serializable

/**
 * Represents a smoke entity.
 *
 * @property timestampMillis The timestamp of the smoke.
 * @property latitude Optional latitude where the smoke was logged.
 * @property longitude Optional longitude where the smoke was logged.
 * @property triggers Predefined [com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger] keys.
 * @property triggerNote Free-text "Other" trigger.
 * @property relationshipSkipped True when the user declared the smoke had no trigger.
 */
@Serializable
data class SmokeEntity(
    val timestampMillis: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val triggers: List<String>? = null,
    val triggerNote: String? = null,
    val relationshipSkipped: Boolean? = null,
) {

    /**
     * Represents the fields of the smoke entity.
     */
    object Fields {
        const val TIMESTAMP_MILLIS = "timestampMillis"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val TRIGGERS = "triggers"
        const val TRIGGER_NOTE = "triggerNote"
        const val RELATIONSHIP_SKIPPED = "relationshipSkipped"
    }
}
