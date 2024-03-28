package com.feragusper.smokeanalytics.features.history.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import java.time.LocalDateTime

sealed class HistoryIntent : MVIIntent {
    data class EditSmoke(val id: String, val date: LocalDateTime) : HistoryIntent()
    data class DeleteSmoke(val id: String) : HistoryIntent()
    data class AddSmoke(val date: LocalDateTime) : HistoryIntent()
    data class FetchSmokes(val date: LocalDateTime) : HistoryIntent()
    object NavigateUp : HistoryIntent()
}
