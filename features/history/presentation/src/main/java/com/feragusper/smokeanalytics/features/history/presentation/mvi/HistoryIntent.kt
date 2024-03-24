package com.feragusper.smokeanalytics.features.history.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import java.util.Date

sealed class HistoryIntent : MVIIntent {
    data class EditSmoke(val id: String, val date: Date) : HistoryIntent()
    data class DeleteSmoke(val id: String) : HistoryIntent()
    data class AddSmoke(val date: Date) : HistoryIntent()
    object FetchSmokes : HistoryIntent()
    object NavigateUp : HistoryIntent()
}
