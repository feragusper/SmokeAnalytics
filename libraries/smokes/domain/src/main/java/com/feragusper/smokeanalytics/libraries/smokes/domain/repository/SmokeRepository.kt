package com.feragusper.smokeanalytics.libraries.smokes.domain.repository

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import java.time.LocalDateTime

/**
 * Interface for the repository managing smoke data. It abstracts the operations related to managing
 * smoke events, allowing for flexibility in the data source implementation.
 */
interface SmokeRepository {

    /**
     * Adds a new smoke event at the specified date and time.
     *
     * @param date The [LocalDateTime] when the smoke event occurred.
     */
    suspend fun addSmoke(date: LocalDateTime)

    /**
     * Fetches smoke events for a given date range.
     *
     * @param startDate The [LocalDateTime] for the start of the date range (optional).
     * @param endDate The [LocalDateTime] for the end of the date range (optional).
     * @return A list of [Smoke] objects representing the fetched smoke events.
     */
    suspend fun fetchSmokes(
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null
    ): List<Smoke>

    /**
     * Fetches the count of smoke events, categorized by day, week, and month.
     *
     * @return A [SmokeCount] object containing the aggregated smoke event data.
     */
    suspend fun fetchSmokeCount(): SmokeCount

    /**
     * Edits an existing smoke event with a new date and time.
     *
     * @param id The unique identifier of the smoke event to be edited.
     * @param date The new [LocalDateTime] for the smoke event.
     */
    suspend fun editSmoke(id: String, date: LocalDateTime)

    /**
     * Deletes a smoke event by its unique identifier.
     *
     * @param id The unique identifier of the smoke event to be deleted.
     */
    suspend fun deleteSmoke(id: String)
}
