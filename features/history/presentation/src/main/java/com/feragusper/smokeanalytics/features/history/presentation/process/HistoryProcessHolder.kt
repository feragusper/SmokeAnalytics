package com.feragusper.smokeanalytics.features.history.presentation.process

import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.catchAndLog
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.smokes.domain.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.FetchSmokesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HistoryProcessHolder @Inject constructor(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val editSmokeUseCase: EditSmokeUseCase,
    private val deleteSmokeUseCase: DeleteSmokeUseCase,
    private val fetchSmokesUseCase: FetchSmokesUseCase,
) : MVIProcessHolder<HistoryIntent, HistoryResult> {

    override fun processIntent(intent: HistoryIntent): Flow<HistoryResult> = when (intent) {
        HistoryIntent.FetchSmokes -> processFetchSmokes()
        is HistoryIntent.AddSmoke -> processAddSmoke(intent)
        is HistoryIntent.EditSmoke -> processEditSmoke(intent)
        is HistoryIntent.DeleteSmoke -> processDeleteSmoke(intent)
        HistoryIntent.NavigateUp -> flow { emit(HistoryResult.NavigateUp) }
    }

    private fun processDeleteSmoke(intent: HistoryIntent.DeleteSmoke) = flow {
        emit(HistoryResult.Loading)
        deleteSmokeUseCase.invoke(intent.id)
        emit(HistoryResult.DeleteSmokeSuccess)
    }.catchAndLog {
        emit(HistoryResult.Error.Generic)
    }

    private fun processEditSmoke(intent: HistoryIntent.EditSmoke) = flow {
        emit(HistoryResult.Loading)
        editSmokeUseCase.invoke(intent.id, intent.date)
        emit(HistoryResult.EditSmokeSuccess)
    }.catchAndLog {
        emit(HistoryResult.Error.Generic)
    }

    private fun processFetchSmokes() = flow {
        emit(HistoryResult.Loading)
        emit(HistoryResult.FetchSmokesSuccess(fetchSmokesUseCase.invoke()))
    }

    private fun processAddSmoke(intent: HistoryIntent.AddSmoke): Flow<HistoryResult> = flow {
        emit(HistoryResult.Loading)
        addSmokeUseCase.invoke(intent.date)
        emit(HistoryResult.AddSmokeSuccess)
    }

}
