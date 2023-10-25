package com.feragusper.smokeanalytics.features.home.presentation.presentation.process

import com.feragusper.smokeanalytics.features.home.domain.AddSmokeUseCase
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeProcessHolder @Inject constructor(
    private val addSmokeUseCase: AddSmokeUseCase,
) : MVIProcessHolder<HomeIntent, HomeResult> {

    override fun processIntent(intent: HomeIntent): Flow<HomeResult> = when (intent) {
        HomeIntent.AddSmoke -> processAddSmoke()
    }

    private fun processAddSmoke(): Flow<HomeResult> = flow {
        emit(HomeResult.Loading)
        addSmokeUseCase.invoke()
        emit(HomeResult.AddSmokeSuccess)
    }.catch {
        emit(HomeResult.AddSmokeError)
    }
}
