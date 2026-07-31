package com.feragusper.smokeanalytics.shared

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Plain, Swift-friendly snapshot of the Home screen state. Uses primitives only (no Kotlin
 * `Instant`, enums or `List<Smoke>`) so SwiftUI can bind to it without interop friction.
 * [lastSmokeEpochMillis] is -1 when there is no recorded smoke.
 */
data class HomeSnapshot(
    val todayCount: Int,
    val weekCount: Int,
    val monthCount: Int,
    val lastSmokeEpochMillis: Long,
)

/**
 * Swift-facing entry point for the Home screen. Sign-in is owned by the Swift side (Google →
 * FirebaseAuth); GitLive's repositories here read the same `Firebase.auth.currentUser`. Both calls
 * assume a signed-in user and throw otherwise (Swift catches via the `@Throws` bridge).
 */
class HomeFacade : KoinComponent {

    private val fetchCounts: FetchSmokeCountListUseCase by inject()
    private val addSmoke: AddSmokeUseCase by inject()

    /** Returns the current aggregated counts for the signed-in user. */
    @Throws(Throwable::class)
    suspend fun load(): HomeSnapshot {
        val result = fetchCounts()
        return HomeSnapshot(
            todayCount = result.todaysSmokes.size,
            weekCount = result.countByWeek,
            monthCount = result.countByMonth,
            lastSmokeEpochMillis = result.lastSmoke?.date?.toEpochMilliseconds() ?: -1L,
        )
    }

    /** Logs a smoke at the current time (no location) and returns the refreshed snapshot. */
    @Throws(Throwable::class)
    suspend fun addSmokeNow(): HomeSnapshot {
        addSmoke()
        return load()
    }
}
