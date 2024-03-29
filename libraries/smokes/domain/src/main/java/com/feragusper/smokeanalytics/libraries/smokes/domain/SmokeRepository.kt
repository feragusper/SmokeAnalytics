package com.feragusper.smokeanalytics.libraries.smokes.domain

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
     * Fetches a list of smoke events, optionally starting from a specified date.
     *
     * @param date The optional [LocalDateTime] to filter smoke events from. If null, fetches all smoke events.
     * @return A list of [Smoke] events.
     */
    suspend fun fetchSmokes(date: LocalDateTime? = null): List<Smoke>

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
