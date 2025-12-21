package com.feragusper.smokeanalytics.features.history.presentation.mvi

import kotlinx.datetime.Instant

sealed interface HistoryIntent {
    data class FetchSmokes(val date: Instant) : HistoryIntent
    data class AddSmoke(val date: Instant) : HistoryIntent
    data class EditSmoke(val id: String, val date: Instant) : HistoryIntent
    data class DeleteSmoke(val id: String) : HistoryIntent
    data object NavigateUp : HistoryIntent
}