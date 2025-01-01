package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIResult

sealed class TileResult : MVIResult {
    data class FetchSmokesSuccess(val smokesPerDay: Int) : TileResult()
    data object Error : TileResult()
}