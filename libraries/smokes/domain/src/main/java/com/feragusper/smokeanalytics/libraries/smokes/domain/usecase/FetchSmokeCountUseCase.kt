package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import javax.inject.Inject

/**
 * Use case for fetching a list of smoke events, optionally filtered by a start date. This operation
 * encapsulates the logic for querying smoke events based on the specified criteria.
 *
 * @property smokeRepository The [SmokeRepository] used for fetching the smoke events.
 */
class FetchSmokeCountUseCase @Inject constructor(
    private val smokeRepository: SmokeRepository
) {

    /**
     * Invokes the use case to fetch smoke events, optionally starting from a specific date.
     *
     * @return A [com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount] object containing the aggregated smoke event data.
     */
    suspend operator fun invoke() = smokeRepository.fetchSmokeCount()
}
