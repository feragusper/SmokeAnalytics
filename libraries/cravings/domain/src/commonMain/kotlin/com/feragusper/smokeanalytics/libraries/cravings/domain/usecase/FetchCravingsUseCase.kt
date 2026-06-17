package com.feragusper.smokeanalytics.libraries.cravings.domain.usecase

import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository
import kotlinx.datetime.Instant

class FetchCravingsUseCase(
    private val cravingRepository: CravingRepository,
) {

    suspend operator fun invoke(
        start: Instant? = null,
        end: Instant? = null,
    ) = cravingRepository.fetchCravings(start, end)
}
