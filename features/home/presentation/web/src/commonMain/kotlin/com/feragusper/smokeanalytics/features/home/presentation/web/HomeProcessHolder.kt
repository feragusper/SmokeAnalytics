package com.feragusper.smokeanalytics.features.home.presentation.web

import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import com.feragusper.smokeanalytics.libraries.logging.AppLogger
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.AddSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.DeleteSmokeUseCase
import com.feragusper.smokeanalytics.libraries.smokes.domain.usecase.EditSmokeUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class HomeProcessHolder(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val editSmokeUseCase: EditSmokeUseCase,
    private val deleteSmokeUseCase: DeleteSmokeUseCase,
    private val fetchSmokeCountListUseCase: FetchSmokeCountListUseCase,
    private val fetchSessionUseCase: FetchSessionUseCase,
) {

    fun processIntent(intent: HomeIntent): Flow<HomeResult> = when (intent) {
        HomeIntent.FetchSmokes -> processFetchSmokes(isRefresh = false)
        HomeIntent.RefreshFetchSmokes -> processFetchSmokes(isRefresh = true)
        HomeIntent.AddSmoke -> processAddSmoke()
        is HomeIntent.EditSmoke -> processEditSmoke(intent)
        is HomeIntent.DeleteSmoke -> processDeleteSmoke(intent)
        HomeIntent.OnClickHistory -> flow { emit(HomeResult.GoToHistory) }
        is HomeIntent.TickTimeSinceLastCigarette -> flow {
            emit(
                HomeResult.UpdateTimeSinceLastCigarette(
                    intent.lastCigarette?.date?.timeElapsedSinceNow() ?: (0L to 0L)
                )
            )
        }
    }

    private fun processFetchSmokes(isRefresh: Boolean): Flow<HomeResult> = flow {
        repeat(5) { attempt ->
            when (fetchSessionUseCase()) {
                is Session.Anonymous -> {
                    emit(HomeResult.NotLoggedIn)

                    // Auth restoration on JS can be async; retry a few times on initial load.
                    if (!isRefresh && attempt < 4) {
                        delay(300)
                        return@repeat
                    } else {
                        return@flow
                    }
                }

                is Session.LoggedIn -> {
                    emit(if (isRefresh) HomeResult.RefreshLoading else HomeResult.Loading)
                    emit(HomeResult.FetchSmokesSuccess(fetchSmokeCountListUseCase()))
                    return@flow
                }
            }
        }
    }.catch {
        emit(HomeResult.FetchSmokesError)
    }

    private fun processAddSmoke(): Flow<HomeResult> = flow {
        AppLogger.d { "AddSmoke intent received" }

        when (val session = fetchSessionUseCase()) {
            is Session.Anonymous -> {
                AppLogger.w { "AddSmoke blocked: user is anonymous" }
                emit(HomeResult.Error.NotLoggedIn)
                emit(HomeResult.GoToAuthentication)
            }

            is Session.LoggedIn -> {
                AppLogger.i { "User logged in, adding smoke..." }
                emit(HomeResult.Loading)
                addSmokeUseCase()
                AppLogger.i { "Smoke added successfully" }
                emit(HomeResult.AddSmokeSuccess)
            }
        }
    }.catch { e ->
        AppLogger.e { "Error adding smoke: ${e.message}" }
        emit(HomeResult.Error.Generic)
    }

    private fun processEditSmoke(intent: HomeIntent.EditSmoke): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        editSmokeUseCase(intent.id, intent.date)
        emit(HomeResult.EditSmokeSuccess)
    }.catch {
        emit(HomeResult.Error.Generic)
    }

    private fun processDeleteSmoke(intent: HomeIntent.DeleteSmoke): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        deleteSmokeUseCase(intent.id)
        emit(HomeResult.DeleteSmokeSuccess)
    }.catch {
        emit(HomeResult.Error.Generic)
    }
}