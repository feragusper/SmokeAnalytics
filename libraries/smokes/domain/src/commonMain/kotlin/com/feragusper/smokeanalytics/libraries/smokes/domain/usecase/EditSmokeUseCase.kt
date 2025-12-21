// commonMain
package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import kotlinx.datetime.Instant

class EditSmokeUseCase(
    private val smokeRepository: SmokeRepository,
) {

    suspend operator fun invoke(id: String, timestamp: Instant) {
        smokeRepository.editSmoke(id, timestamp)
    }
}