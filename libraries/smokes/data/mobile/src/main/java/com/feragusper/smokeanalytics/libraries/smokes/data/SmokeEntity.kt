package com.feragusper.smokeanalytics.libraries.smokes.data

/**
 * Firestore entity for a smoke event.
 *
 * Schema (Android + Web):
 *  - timestampMillis: Double (epoch millis)
 *  - latitude / longitude: Double? (optional location)
 *  - triggers: List<String>? (predefined [com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeTrigger] keys)
 *  - triggerNote: String? (free-text "Other")
 *  - relationshipSkipped: Boolean? (true when the user said "no relation")
 *
 * The relationship fields are all optional, so documents written before this feature
 * (and smokes added from the watch) load as untracked.
 */
data class SmokeEntity(
    val timestampMillis: Double = 0.0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val triggers: List<String>? = null,
    val triggerNote: String? = null,
    val relationshipSkipped: Boolean? = null,
) {
    object Fields {
        const val TIMESTAMP_MILLIS = "timestampMillis"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val TRIGGERS = "triggers"
        const val TRIGGER_NOTE = "triggerNote"
        const val RELATIONSHIP_SKIPPED = "relationshipSkipped"
    }
}
