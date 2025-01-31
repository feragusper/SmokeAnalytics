package com.feragusper.smokeanalytics.features.history.presentation.process

import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.catchAndLog
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.smokes.domain.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.FetchSmokeCountUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.FetchSmokesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HistoryProcessHolder @Inject constructor(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val editSmokeUseCase: EditSmokeUseCase,
    private val deleteSmokeUseCase: DeleteSmokeUseCase,
    private val fetchSmokesUseCase: FetchSmokesUseCase,
    private val fetchSessionUseCase: FetchSessionUseCase,
) : MVIProcessHolder<HistoryIntent, HistoryResult> {

    override fun processIntent(intent: HistoryIntent): Flow<HistoryResult> = when (intent) {
        is HistoryIntent.FetchSmokes -> processFetchSmokes(intent)
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

    private fun processFetchSmokes(intent: HistoryIntent.FetchSmokes) = flow {
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> emit(HistoryResult.NotLoggedIn(intent.date))
            is Session.LoggedIn -> {
                emit(HistoryResult.Loading)
                emit(
                    HistoryResult.FetchSmokesSuccess(
                        selectedDate = intent.date,
                        smokes = fetchSmokesUseCase.invoke(intent.date)
                    )
                )
            }
        }
    }.catchAndLog {
        emit(HistoryResult.FetchSmokesError)
    }

    private fun processAddSmoke(intent: HistoryIntent.AddSmoke): Flow<HistoryResult> = flow {
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> {
                emit(HistoryResult.Error.NotLoggedIn)
                emit(HistoryResult.GoToAuthentication)
            }

            is Session.LoggedIn -> {
                emit(HistoryResult.Loading)
                addSmokeUseCase.invoke(intent.date)
                emit(HistoryResult.AddSmokeSuccess)
            }
        }
    }.catchAndLog {
        emit(HistoryResult.Error.Generic)
    }

}
