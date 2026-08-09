package com.feragusper.smokeanalytics.libraries.cravings.domain.usecase

import com.feragusper.smokeanalytics.libraries.architecture.domain.DataSource
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository

class FetchActiveCravingUseCase(
    private val cravingRepository: CravingRepository,
) {

    suspend operator fun invoke(
        source: DataSource = DataSource.DEFAULT,
    ): Craving? = cravingRepository.fetchActiveCraving(source)
}
