package com.feragusper.smokeanalytics.features.home.presentation.process

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.catchAndLog
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.smokes.domain.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.EditSmokeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Processes intents from the Home feature, invoking the appropriate use cases and updating the result.
 *
 * @param addSmokeUseCase Use case for adding a new smoke entry.
 * @param editSmokeUseCase Use case for editing an existing smoke entry.
 * @param deleteSmokeUseCase Use case for deleting an existing smoke entry.
 * @param fetchSmokeCountListUseCase Use case for fetching smoke counts and latest smokes.
 * @param fetchSessionUseCase Use case for fetching the current session state.
 */
class HomeProcessHolder @Inject constructor(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val editSmokeUseCase: EditSmokeUseCase,
    private val deleteSmokeUseCase: DeleteSmokeUseCase,
    private val fetchSmokeCountListUseCase: FetchSmokeCountListUseCase,
    private val fetchSessionUseCase: FetchSessionUseCase,
) : MVIProcessHolder<HomeIntent, HomeResult> {

    override fun processIntent(intent: HomeIntent): Flow<HomeResult> = when (intent) {
        HomeIntent.FetchSmokes, HomeIntent.RefreshFetchSmokes -> processFetchSmokes(intent is HomeIntent.RefreshFetchSmokes)
        HomeIntent.AddSmoke -> processAddSmoke()
        HomeIntent.OnClickHistory -> flow { emit(HomeResult.GoToHistory) }
        is HomeIntent.TickTimeSinceLastCigarette -> processTickTimeSinceLastCigarette(intent)
        is HomeIntent.EditSmoke -> processEditSmoke(intent)
        is HomeIntent.DeleteSmoke -> processDeleteSmoke(intent)
    }

    private fun processFetchSmokes(isRefresh: Boolean) = flow {
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> emit(HomeResult.NotLoggedIn)
            is Session.LoggedIn -> {
                emit(if (isRefresh) HomeResult.RefreshLoading else HomeResult.Loading)
                emit(HomeResult.FetchSmokesSuccess(fetchSmokeCountListUseCase.invoke()))
            }
        }
    }.catchAndLog {
        emit(HomeResult.FetchSmokesError)
    }

    private fun processDeleteSmoke(intent: HomeIntent.DeleteSmoke) = flow {
        emit(HomeResult.Loading)
        deleteSmokeUseCase.invoke(intent.id)
        emit(HomeResult.DeleteSmokeSuccess)
    }.catchAndLog {
        emit(HomeResult.Error.Generic)
    }

    private fun processEditSmoke(intent: HomeIntent.EditSmoke) = flow {
        emit(HomeResult.Loading)
        editSmokeUseCase.invoke(intent.id, intent.date)
        emit(HomeResult.EditSmokeSuccess)
    }.catchAndLog {
        emit(HomeResult.Error.Generic)
    }

    private fun processTickTimeSinceLastCigarette(intent: HomeIntent.TickTimeSinceLastCigarette) =
        flow {
            emit(
                HomeResult.UpdateTimeSinceLastCigarette(
                    intent.lastCigarette?.date?.timeElapsedSinceNow() ?: (0L to 0L)
                )
            )
        }

    private fun processAddSmoke(): Flow<HomeResult> = flow {
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> {
                emit(HomeResult.Error.NotLoggedIn)
                emit(HomeResult.GoToAuthentication)
            }

            is Session.LoggedIn -> {
                emit(HomeResult.Loading)
                addSmokeUseCase.invoke()
                emit(HomeResult.AddSmokeSuccess)
            }
        }
    }.catchAndLog {
        emit(HomeResult.Error.Generic)
    }
}
