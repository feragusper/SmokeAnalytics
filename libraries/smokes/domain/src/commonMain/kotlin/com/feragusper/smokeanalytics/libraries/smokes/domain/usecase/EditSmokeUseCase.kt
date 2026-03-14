// commonMain
package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import kotlinx.datetime.Instant

class EditSmokeUseCase(
    private val smokeRepository: SmokeRepository,
) {

    suspend operator fun invoke(id: String, timestamp: Instant, location: GeoPoint? = null) {
        smokeRepository.editSmoke(id, timestamp, location)
    }
}
