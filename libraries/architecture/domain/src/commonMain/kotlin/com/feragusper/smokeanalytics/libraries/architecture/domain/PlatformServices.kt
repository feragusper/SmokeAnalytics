package com.feragusper.smokeanalytics.libraries.architecture.domain

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
)

interface LocationCaptureService {
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
    val weekCount: Int,
    val monthCount: Int,
    val currentStreakHours: Long,
    val elapsedHours: Long,
    val elapsedMinutes: Long,
    val spentToday: Double,
)

interface WidgetRefreshService {
    suspend fun refreshHomeSnapshot(snapshot: WidgetSnapshot)
}
