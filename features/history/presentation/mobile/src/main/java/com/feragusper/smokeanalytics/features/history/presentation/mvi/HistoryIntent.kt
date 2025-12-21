package com.feragusper.smokeanalytics.features.history.presentation.mvi

import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIIntent
import kotlinx.datetime.Instant

sealed class HistoryIntent : MVIIntent {

    data class EditSmoke(val id: String, val date: Instant) : HistoryIntent()

    data class DeleteSmoke(val id: String) : HistoryIntent()

    data class AddSmoke(val date: Instant) : HistoryIntent()

    data class FetchSmokes(val date: Instant) : HistoryIntent()

    data object NavigateUp : HistoryIntent()
}