package com.feragusper.smokeanalytics.features.home.presentation.presentation.process

import com.feragusper.smokeanalytics.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeIntent
import com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi.HomeResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class HomeProcessHolder @Inject constructor() : MVIProcessHolder<HomeIntent, HomeResult> {

    override fun processIntent(intent: HomeIntent): Flow<HomeResult> = flowOf()

}
