package com.feragusper.smokeanalytics.features.history.presentation.process

import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryIntent
import com.feragusper.smokeanalytics.features.history.presentation.mvi.HistoryResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.catchAndLog
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.FetchSmokesUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.SyncWithWearUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Handles the business logic of processing intents for the History feature.
 *
 * This class is responsible for transforming [HistoryIntent] into [HistoryResult]s,
 * encapsulating the application's business logic for managing smoke history.
 *
 * @property addSmokeUseCase Use case for adding a smoke event.
 * @property editSmokeUseCase Use case for editing a smoke event.
 * @property deleteSmokeUseCase Use case for deleting a smoke event.
 * @property fetchSmokesUseCase Use case for fetching smoke events.
 * @property fetchSessionUseCase Use case for fetching the current session information.
 */
class HistoryProcessHolder @Inject constructor(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val editSmokeUseCase: EditSmokeUseCase,
    private val deleteSmokeUseCase: DeleteSmokeUseCase,
    private val fetchSmokesUseCase: FetchSmokesUseCase,
    private val fetchSessionUseCase: FetchSessionUseCase,
    private val syncWithWearUseCase: SyncWithWearUseCase
) : MVIProcessHolder<HistoryIntent, HistoryResult> {

    /**
     * Processes an [HistoryIntent] and transforms it into a stream of [HistoryResult]s.
     *
     * @param intent The user intent to be processed.
     * @return A [Flow] emitting the corresponding [HistoryResult]s.
     */
    override fun processIntent(intent: HistoryIntent): Flow<HistoryResult> = when (intent) {
        is HistoryIntent.FetchSmokes -> processFetchSmokes(intent)
        is HistoryIntent.AddSmoke -> processAddSmoke(intent)
        is HistoryIntent.EditSmoke -> processEditSmoke(intent)
        is HistoryIntent.DeleteSmoke -> processDeleteSmoke(intent)
        HistoryIntent.NavigateUp -> flow { emit(HistoryResult.NavigateUp) }
    }

    /**
     * Handles the [HistoryIntent.DeleteSmoke] intent by deleting the specified smoke event.
     *
     * @param intent The delete smoke intent containing the smoke ID to be deleted.
     * @return A [Flow] emitting the result of the delete operation.
     */
    private fun processDeleteSmoke(intent: HistoryIntent.DeleteSmoke) = flow {
        emit(HistoryResult.Loading)
        deleteSmokeUseCase.invoke(intent.id)
        emit(HistoryResult.DeleteSmokeSuccess)
        syncWithWearUseCase.invoke()
    }.catchAndLog {
        emit(HistoryResult.Error.Generic)
    }

    /**
     * Handles the [HistoryIntent.EditSmoke] intent by editing the specified smoke event.
     *
     * @param intent The edit smoke intent containing the smoke ID and new date.
     * @return A [Flow] emitting the result of the edit operation.
     */
    private fun processEditSmoke(intent: HistoryIntent.EditSmoke) = flow {
        emit(HistoryResult.Loading)
        editSmokeUseCase.invoke(intent.id, intent.date)
        emit(HistoryResult.EditSmokeSuccess)
        syncWithWearUseCase.invoke()
    }.catchAndLog {
        emit(HistoryResult.Error.Generic)
    }

    /**
     * Handles the [HistoryIntent.FetchSmokes] intent by fetching the list of smoke events
     * for the specified date. It checks the user's session state before fetching.
     *
     * @param intent The fetch smoke intent containing the selected date.
     * @return A [Flow] emitting the result of the fetch operation.
     */
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

    /**
     * Handles the [HistoryIntent.AddSmoke] intent by adding a new smoke event for the specified date.
     * It checks the user's session state before adding the smoke.
     *
     * @param intent The add smoke intent containing the date of the smoke event.
     * @return A [Flow] emitting the result of the add operation.
     */
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
                syncWithWearUseCase.invoke()
            }
        }
    }.catchAndLog {
        emit(HistoryResult.Error.Generic)
    }
}
