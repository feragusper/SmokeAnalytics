package com.feragusper.smokeanalytics.features.history.presentation

import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class HistoryViewState(
    val displayLoading: Boolean = false,
    val smokes: List<Smoke> = emptyList(),
    val selectedDate: Instant = Clock.System.now(),
    val error: HistoryResult.Error? = null,
)