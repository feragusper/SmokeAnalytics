package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for fetching a list of smoke events, optionally filtered by a start date. This operation
 * encapsulates the logic for querying smoke events based on the specified criteria.
 *
 * @property smokeRepository The [SmokeRepository] used for fetching the smoke events.
 */
class FetchSmokesUseCase @Inject constructor(
    private val smokeRepository: SmokeRepository
) {

    /**
     * Invokes the use case to fetch smoke events, optionally starting from a specific date.
     *
     * @param startDate The optional [LocalDateTime] to filter smoke events from.
     * @param endDate The optional [LocalDateTime] to filter smoke events up to.
     * @return A list of [com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke] events.
     */
    suspend operator fun invoke(
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null
    ) = smokeRepository.fetchSmokes(startDate, endDate)
}
