package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState

// Data class to represent the state of the Tile
data class TileViewState(
    val smokesPerDay: Int? = null, // The number of smokes for the current day
    val smokesPerWeek: Int? = null, // The number of smokes for the current week
    val smokesPerMonth: Int? = null, // The number of smokes for the current month
    val lastSmokeTimestamp: Long? = null, // The timestamp for the last smoke
    val error: TileResult? = null // Represents any error that might have occurred
) : MVIViewState<TileIntent>
