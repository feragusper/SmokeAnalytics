package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.libraries.architecture.domain.Coordinate
import com.feragusper.smokeanalytics.libraries.architecture.domain.LocationCaptureService

/**
 * Placeholder location service for iOS. Returns no fix and reports availability purely from the
 * user preference, so the domain keeps working before CoreLocation is wired up (Phase 2).
 */
internal class IosLocationCaptureService : LocationCaptureService {
    override suspend fun captureCurrentLocation(): Coordinate? = null
}
