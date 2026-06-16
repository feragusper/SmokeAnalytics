package com.feragusper.smokeanalytics.libraries.cravings.domain.usecase

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository

class FetchActiveCravingUseCase(
    private val cravingRepository: CravingRepository,
) {

    suspend operator fun invoke(): Craving? = cravingRepository.fetchActiveCraving()
}
