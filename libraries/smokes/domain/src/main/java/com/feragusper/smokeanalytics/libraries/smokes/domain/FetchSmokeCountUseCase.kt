package com.feragusper.smokeanalytics.libraries.smokes.domain

import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for fetching a list of smoke events, optionally filtered by a start date. This operation
 * encapsulates the logic for querying smoke events based on the specified criteria.
 *
 * @property smokeRepository The [SmokeRepository] used for fetching the smoke events.
 */
data class FetchSmokeCountUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {

    /**
     * Invokes the use case to fetch smoke events, optionally starting from a specific date.
     *
     * @return A [SmokeCount] object containing the aggregated smoke event data.
     */
    suspend operator fun invoke() = smokeRepository.fetchSmokeCount()
}
