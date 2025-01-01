package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TileViewModel @Inject constructor(
    private val processHolder: TileProcessHolder,
) : MVIViewModel<TileIntent, TileViewState, TileResult, MVINavigator>(
    initialState = TileViewState()
) {
    override lateinit var navigator: MVINavigator

    init {
        Timber.d("TileViewModel", "init")
        intents().trySend(TileIntent.FetchSmokes)
    }

    override suspend fun transformer(intent: TileIntent): Flow<TileResult> =
        processHolder.processIntent(intent)

    override suspend fun reducer(
        previous: TileViewState,
        result: TileResult
    ): TileViewState = when (result) {
        is TileResult.FetchSmokesSuccess -> previous.copy(smokesPerDay = result.smokesPerDay)
        is TileResult.Error -> previous.copy(error = result)
    }
}