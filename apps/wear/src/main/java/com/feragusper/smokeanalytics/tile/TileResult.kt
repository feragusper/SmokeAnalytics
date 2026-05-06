package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

sealed class TileResult : MVIResult {
    data class AddSmokeStarted(val requestedAtMillis: Long) : TileResult()

    data object RefreshStarted : TileResult()

    data class FetchSmokesSuccess(
        val todayCount: Int,
        val targetGapMinutes: Int,
        val averageSmokesPerDayWeek: Double,
        val lastSmokeTimestamp: Long?,
    ) : TileResult()

    data object AddSmokeRequestSent : TileResult()

    data object RefreshRequestSent : TileResult()

    data object Error : TileResult()
}
