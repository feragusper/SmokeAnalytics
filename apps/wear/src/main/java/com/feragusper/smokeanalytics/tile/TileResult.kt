package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

sealed class TileResult : MVIResult {
    data class FetchSmokesSuccess(
        val smokesPerDay: Int,
        val smokesPerWeek: Int,
        val smokesPerMonth: Int,
        val lastSmokeTimestamp: Long?
    ) : TileResult()

    data object AddSmokeSuccess : TileResult()

    data object Error : TileResult()
}