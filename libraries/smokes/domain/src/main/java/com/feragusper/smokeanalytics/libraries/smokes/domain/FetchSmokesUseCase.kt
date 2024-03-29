package com.feragusper.smokeanalytics.libraries.smokes.domain

import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for fetching a list of smoke events, optionally filtered by a start date. This operation
 * encapsulates the logic for querying smoke events based on the specified criteria.
 *
 * @property smokeRepository The [SmokeRepository] used for fetching the smoke events.
 */
data class FetchSmokesUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {

    /**
     * Invokes the use case to fetch smoke events, optionally starting from a specific date.
     *
     * @param date The optional [LocalDateTime] to filter smoke events from. If null, all smoke events are fetched.
     * @return A list of [Smoke] events.
     */
    suspend operator fun invoke(date: LocalDateTime? = null) = smokeRepository.fetchSmokes(date)
}
