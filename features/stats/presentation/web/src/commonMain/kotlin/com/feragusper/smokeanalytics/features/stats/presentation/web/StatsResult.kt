package com.feragusper.smokeanalytics.features.stats.presentation.web

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeStats

sealed interface StatsResult {
    data object Loading : StatsResult
    data class Success(val stats: SmokeStats) : StatsResult
    data class Error(val error: Throwable) : StatsResult
}