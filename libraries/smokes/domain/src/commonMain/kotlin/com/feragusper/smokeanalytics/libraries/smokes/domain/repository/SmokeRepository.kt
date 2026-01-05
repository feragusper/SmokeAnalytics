package com.feragusper.smokeanalytics.libraries.smokes.domain.repository

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import kotlinx.datetime.Instant

/**
 * Represents a smoke repository.
 */
interface SmokeRepository {

    /**
     * Adds a smoke.
     *
     * @param timestamp The timestamp of the smoke.
     */
    suspend fun addSmoke(timestamp: Instant)

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
    suspend fun fetchSmokeCount(): SmokeCount

    /**
     * Edits a smoke.
     *
     * @param id The id of the smoke.
     * @param timestamp The timestamp of the smoke.
     */
    suspend fun editSmoke(id: String, timestamp: Instant)

    /**
     * Deletes a smoke.
     *
     * @param id The id of the smoke.
     */
    suspend fun deleteSmoke(id: String)
}