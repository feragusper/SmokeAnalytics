package com.feragusper.smokeanalytics.libraries.smokes.domain.repository

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeRelationship
import kotlinx.datetime.Instant

/**
 * Represents a smoke repository.
 */
interface SmokeRepository {

    /**
     * Adds a smoke.
     *
     * @param timestamp The timestamp of the smoke.
     * @return The id of the newly created smoke, so callers can attach a relationship to it.
     */
    suspend fun addSmoke(timestamp: Instant, location: GeoPoint? = null): String

    /**
     * Sets (or clears) what a smoke was related to.
     *
     * Only the relationship fields are written; the timestamp and location are preserved.
     *
     * @param id The id of the smoke.
     * @param relationship The relationship to persist.
     */
    suspend fun setSmokeRelationship(id: String, relationship: SmokeRelationship)

    /**
     * Fetches the smokes.
     *
     * @param start The start date.
     * @param end The end date.
     *
     * @return The smokes.
     */
    suspend fun fetchSmokes(
        start: Instant? = null,
        end: Instant? = null,
    ): List<Smoke>

    /**
     * Fetches the smoke count.
     *
     * @return The smoke count.
     */
    suspend fun fetchSmokeCount(
        dayStartHour: Int = 0,
        manualDayStartEpochMillis: Long? = null,
    ): SmokeCount

    /**
     * Edits a smoke.
     *
     * @param id The id of the smoke.
     * @param timestamp The timestamp of the smoke.
     */
    suspend fun editSmoke(id: String, timestamp: Instant, location: GeoPoint? = null)

    /**
     * Deletes a smoke.
     *
     * @param id The id of the smoke.
     */
    suspend fun deleteSmoke(id: String)
}
