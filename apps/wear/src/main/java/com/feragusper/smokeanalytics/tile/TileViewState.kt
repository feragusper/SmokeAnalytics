package com.feragusper.smokeanalytics.tile

import androidx.compose.runtime.Composable
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState

data class TileViewState(
    val smokesPerDay: Int? = null,
    val smokesPerWeek: Int? = null,
    val smokesPerMonth: Int? = null,
    val lastSmokeTimestamp: Long? = null,
    val error: TileResult? = null
) : MVIViewState<TileIntent> {
    @Composable
    override fun Compose(intent: (TileIntent) -> Unit) {
        // no-op
    }
}