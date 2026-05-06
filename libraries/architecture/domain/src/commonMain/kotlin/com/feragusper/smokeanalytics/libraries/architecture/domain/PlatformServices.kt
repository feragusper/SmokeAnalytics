package com.feragusper.smokeanalytics.libraries.architecture.domain

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
)

data class LocationTrackingAvailability(
    val preferenceEnabled: Boolean,
    val permissionGranted: Boolean,
    val providerEnabled: Boolean,
) {
    val isReady: Boolean
        get() = preferenceEnabled && permissionGranted && providerEnabled
}

interface LocationCaptureService {
    suspend fun locationTrackingAvailability(preferenceEnabled: Boolean): LocationTrackingAvailability =
        LocationTrackingAvailability(
            preferenceEnabled = preferenceEnabled,
            permissionGranted = preferenceEnabled,
            providerEnabled = preferenceEnabled,
        )

    suspend fun captureCurrentLocation(): Coordinate?
}

interface ShareService {
    suspend fun share(title: String, text: String, url: String?)
}

interface ReviewPromptService {
    suspend fun requestReview()
}

interface ExternalLinkService {
    suspend fun open(url: String)
}

data class WidgetSnapshot(
    val todayCount: Int,
    val elapsedHours: Long,
    val elapsedMinutes: Long,
    val targetGapMinutes: Int,
    val averageSmokesPerDayWeek: Double,
)

interface WidgetRefreshService {
    suspend fun refreshHomeSnapshot(snapshot: WidgetSnapshot)
}
