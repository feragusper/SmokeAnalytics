package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState

// Data class to represent the state of the Tile
data class TileViewState(
    val todayCount: Int? = null,
    val targetGapMinutes: Int? = null,
    val averageSmokesPerDayWeek: Double? = null,
    val lastSmokeTimestamp: Long? = null,
    val refreshRequestInFlight: Boolean = false,
    val addSmokePendingCount: Int = 0,
    val addSmokePendingBaseline: TileSmokeSnapshot? = null,
    val error: TileResult? = null,
) : MVIViewState<TileIntent>

data class TileSmokeSnapshot(
    val todayCount: Int?,
    val targetGapMinutes: Int?,
    val averageSmokesPerDayWeek: Double?,
    val lastSmokeTimestamp: Long?,
)

fun TileViewState.withOptimisticAddSmoke(requestedAtMillis: Long): TileViewState {
    val countBefore = todayCount ?: 0
    val countAfter = countBefore + 1
    val baseline = addSmokePendingBaseline ?: TileSmokeSnapshot(
        todayCount = todayCount,
        targetGapMinutes = targetGapMinutes,
        averageSmokesPerDayWeek = averageSmokesPerDayWeek,
        lastSmokeTimestamp = lastSmokeTimestamp,
    )

    return copy(
        todayCount = countAfter,
        targetGapMinutes = targetGapMinutes,
        averageSmokesPerDayWeek = averageSmokesPerDayWeek?.plus(1.0 / 7.0),
        lastSmokeTimestamp = requestedAtMillis,
        addSmokePendingCount = addSmokePendingCount + 1,
        addSmokePendingBaseline = baseline,
        error = null,
    )
}

fun TileViewState.withPendingAddSmokeRolledBack(): TileViewState {
    val baseline = addSmokePendingBaseline ?: return copy(
        refreshRequestInFlight = false,
        addSmokePendingCount = 0,
        error = TileResult.Error,
    )

    return copy(
        todayCount = baseline.todayCount,
        targetGapMinutes = baseline.targetGapMinutes,
        averageSmokesPerDayWeek = baseline.averageSmokesPerDayWeek,
        lastSmokeTimestamp = baseline.lastSmokeTimestamp,
        refreshRequestInFlight = false,
        addSmokePendingCount = 0,
        addSmokePendingBaseline = null,
        error = TileResult.Error,
    )
}
