package com.feragusper.smokeanalytics.features.home.presentation.presentation.process

import com.feragusper.smokeanalytics.features.home.domain.AddSmokeUseCase
import com.feragusper.smokeanalytics.features.home.domain.FetchSmokeCountListUseCase
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeProcessHolder @Inject constructor(
    private val addSmokeUseCase: AddSmokeUseCase,
    private val fetchSmokeCountListUseCase: FetchSmokeCountListUseCase,
) : MVIProcessHolder<HomeIntent, HomeResult> {

    override fun processIntent(intent: HomeIntent): Flow<HomeResult> = when (intent) {
        HomeIntent.AddSmoke -> processAddSmoke()
        HomeIntent.FetchSmokes -> processFetchSmokes()
    }

    private fun processFetchSmokes() = flow {
        emit(HomeResult.Loading)
        emit(HomeResult.FetchSmokesSuccess(fetchSmokeCountListUseCase.invoke()))
    }.catch {
        emit(HomeResult.FetchSmokesError)
    }

    private fun processAddSmoke(): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        addSmokeUseCase.invoke()
        emit(HomeResult.AddSmokeSuccess)
    }.catch {
        emit(HomeResult.AddSmokeError)
    }
}
