package com.feragusper.smokeanalytics.tile

import android.annotation.SuppressLint
import android.content.Context
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator
import com.feragusper.smokeanalytics.libraries.wear.data.WearSyncManager
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

object TileViewModel : MVIViewModel<TileIntent, TileViewState, TileResult, MVINavigator>(
    initialState = TileViewState()
) {

    @SuppressLint("StaticFieldLeak")
    private lateinit var wearSyncManager: WearSyncManager

    private val processHolder: TileProcessHolder by lazy {
        TileProcessHolder(wearSyncManager)
    }

    override lateinit var navigator: MVINavigator

    fun initialize(context: Context) {
        wearSyncManager = WearSyncManager(context)
    }

    init {
        Timber.d("init")
        intents().trySend(TileIntent.FetchSmokes)
    }

    override suspend fun transformer(intent: TileIntent): Flow<TileResult> =
        processHolder.processIntent(intent)

    override suspend fun reducer(
        previous: TileViewState,
        result: TileResult
    ): TileViewState = when (result) {
        is TileResult.FetchSmokesSuccess -> previous.copy(
            smokesPerDay = result.smokesPerDay,
            smokesPerWeek = result.smokesPerWeek,
            smokesPerMonth = result.smokesPerMonth,
            lastSmokeTimestamp = result.lastSmokeTimestamp
        )

        is TileResult.Error -> previous.copy(error = result)
    }
}