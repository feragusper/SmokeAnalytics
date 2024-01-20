package com.feragusper.smokeanalytics.features.home.presentation.presentation.process

import com.feragusper.smokeanalytics.features.home.domain.AddSmokeUseCase
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.timeElapsedSinceNow
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.authentication.domain.FetchSessionUseCase
import com.feragusper.smokeanalytics.libraries.authentication.domain.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeProcessHolder @Inject constructor(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val fetchSmokeCountListUseCase: FetchSmokeCountListUseCase,
    private val fetchSessionUseCase: FetchSessionUseCase,
) : MVIProcessHolder<HomeIntent, HomeResult> {

    override fun processIntent(intent: HomeIntent): Flow<HomeResult> = when (intent) {
        HomeIntent.AddSmoke -> processAddSmoke()
        HomeIntent.FetchSmokes -> processFetchSmokes()
        is HomeIntent.TickTimeSinceLastCigarette -> processTickTimeSinceLastCigarette(intent)
    }

    private fun processTickTimeSinceLastCigarette(intent: HomeIntent.TickTimeSinceLastCigarette) =
        flow {
            emit(
                HomeResult.UpdateTimeSinceLastCigarette(
                    intent.lastCigarette?.date?.timeElapsedSinceNow() ?: (0L to 0L)
                )
            )
        }

    private fun processFetchSmokes() = flow {
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> emit(HomeResult.NotLoggedIn)
            is Session.LoggedIn -> {
                emit(HomeResult.Loading)
                emit(HomeResult.FetchSmokesSuccess(fetchSmokeCountListUseCase.invoke()))
            }
        }
    }.catch {
        emit(HomeResult.FetchSmokesError)
    }

    private fun processAddSmoke(): Flow<HomeResult> = flow {
        when (fetchSessionUseCase()) {
            is Session.Anonymous -> {
                emit(HomeResult.Error.NotLoggedIn)
                emit(HomeResult.GoToLogin)
            }

            is Session.LoggedIn -> {
                emit(HomeResult.Loading)
                addSmokeUseCase.invoke()
                emit(HomeResult.AddSmokeSuccess)
            }
        }
    }.catch {
        emit(HomeResult.Error.Generic)
    }
}
