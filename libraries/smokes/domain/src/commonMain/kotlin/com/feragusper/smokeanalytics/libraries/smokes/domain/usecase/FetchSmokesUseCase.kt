// commonMain
package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.architecture.domain.DataSource
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import kotlinx.datetime.Instant

class FetchSmokesUseCase(
    private val smokeRepository: SmokeRepository,
) {

    suspend operator fun invoke(
        start: Instant? = null,
        end: Instant? = null,
        source: DataSource = DataSource.DEFAULT,
    ) = smokeRepository.fetchSmokes(start, end, source)
}