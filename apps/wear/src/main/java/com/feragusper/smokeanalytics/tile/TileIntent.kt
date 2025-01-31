package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent

sealed class TileIntent : MVIIntent {
    data object FetchSmokes : TileIntent()
    data object AddSmoke : TileIntent()
}