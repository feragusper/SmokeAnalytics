package com.feragusper.smokeanalytics.libraries.cravings.domain.usecase

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository
import kotlinx.datetime.Instant
import kotlin.time.Clock

class AddCravingUseCase(
    private val cravingRepository: CravingRepository,
) {

    suspend operator fun invoke(
        createdAt: Instant = Clock.System.now(),
        targetAt: Instant? = null,
    ): Craving = cravingRepository.addCraving(createdAt, targetAt)
}
