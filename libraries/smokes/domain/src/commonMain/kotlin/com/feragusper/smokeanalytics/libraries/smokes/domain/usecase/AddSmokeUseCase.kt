// commonMain
package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class AddSmokeUseCase(
    private val smokeRepository: SmokeRepository,
) {

    suspend operator fun invoke(timestamp: Instant = Clock.System.now()) {
        smokeRepository.addSmoke(timestamp)
    }
}