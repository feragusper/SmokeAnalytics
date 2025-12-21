package com.feragusper.smokeanalytics.features.stats.presentation.web

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats

data class StatsViewState(
    val displayLoading: Boolean = false,
    val stats: SmokeStats? = null,
    val error: StatsError? = null,
) {
    sealed interface StatsError {
        data object Generic : StatsError
    }
}