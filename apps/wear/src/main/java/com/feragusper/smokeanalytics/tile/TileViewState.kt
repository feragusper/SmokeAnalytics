package com.feragusper.smokeanalytics.tile

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState

data class TileViewState(
    val smokesPerDay: Int? = null,
    val error: TileResult? = null
) : MVIViewState<TileIntent> {
    @Composable
    override fun Compose(intent: (TileIntent) -> Unit) {
        // No UI necesaria en Wear, los Tiles usan esto indirectamente.
    }
}