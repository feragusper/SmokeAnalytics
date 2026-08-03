package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** A logged cigarette that has a location, for the map. */
data class SmokeLocationDTO(val latitude: Double, val longitude: Double, val epochMillis: Long)

/** Swift-facing entry point for the map: the located cigarettes of the last year. */
class MapFacade : KoinComponent {

    private val fetchSmokes: FetchSmokesUseCase by inject()

    @Throws(Throwable::class)
    suspend fun locatedSmokes(): List<SmokeLocationDTO> =
        fetchSmokes(Clock.System.now().minus(365.days), null).mapNotNull { smoke ->
            smoke.location?.let {
                SmokeLocationDTO(latitude = it.latitude, longitude = it.longitude, epochMillis = smoke.date.toEpochMilliseconds())
            }
        }
}
