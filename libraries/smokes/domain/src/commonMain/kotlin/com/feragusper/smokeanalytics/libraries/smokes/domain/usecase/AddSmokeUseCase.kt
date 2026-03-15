// commonMain
package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class AddSmokeUseCase(
    private val smokeRepository: SmokeRepository,
) {

    suspend operator fun invoke(
        timestamp: Instant = Clock.System.now(),
        location: GeoPoint? = null,
    ) {
        smokeRepository.addSmoke(timestamp, location)
    }
}
