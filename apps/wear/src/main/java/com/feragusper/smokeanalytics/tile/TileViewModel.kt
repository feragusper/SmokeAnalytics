package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator
import com.feragusper.smokeanalytics.libraries.wear.data.WearSyncManager
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

object TileViewModel : MVIViewModel<TileIntent, TileViewState, TileResult, MVINavigator>(
    initialState = TileViewState()
) {
    private val processHolder: TileProcessHolder =
        TileProcessHolder(WearSyncManager(SmokeAnalyticsApplication.instance))

    override lateinit var navigator: MVINavigator

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
        is TileResult.FetchSmokesSuccess -> previous.copy(smokesPerDay = result.smokesPerDay).also {
            Timber.d("Reducer: $it")
        }

        is TileResult.Error -> previous.copy(error = result).also {
            Timber.d("Reducer: $it")
        }
    }
}