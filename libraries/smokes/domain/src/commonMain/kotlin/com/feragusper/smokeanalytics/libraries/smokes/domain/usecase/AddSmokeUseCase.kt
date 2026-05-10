// commonMain
package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
import kotlinx.datetime.Instant
import kotlin.time.Clock

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
