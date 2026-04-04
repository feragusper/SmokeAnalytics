package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState

// Data class to represent the state of the Tile
data class TileViewState(
    val todayCount: Int? = null,
    val targetGapMinutes: Int? = null,
    val averageSmokesPerDayWeek: Double? = null,
    val lastSmokeTimestamp: Long? = null,
    val error: TileResult? = null,
) : MVIViewState<TileIntent>
