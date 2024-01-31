package com.feragusper.smokeanalytics.features.stats.presentation.process

import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsIntent
import com.feragusper.smokeanalytics.features.stats.presentation.mvi.StatsResult
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class StatsProcessHolder @Inject constructor() : MVIProcessHolder<StatsIntent, StatsResult> {

    override fun processIntent(intent: StatsIntent): Flow<StatsResult> = flowOf()

}
