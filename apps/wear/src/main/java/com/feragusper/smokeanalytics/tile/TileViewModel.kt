package com.feragusper.smokeanalytics.tile

import android.annotation.SuppressLint
import android.content.Context
import com.feragusper.smokeanalytics.libraries.architecture.presentation.MVIViewModel
import com.feragusper.smokeanalytics.libraries.architecture.presentation.navigation.MVINavigator
import com.feragusper.smokeanalytics.libraries.wear.data.WearSyncManager
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

// ViewModel for the Tile feature, responsible for managing the state and handling intents
object TileViewModel : MVIViewModel<TileIntent, TileViewState, TileResult, MVINavigator>(
    initialState = TileViewState()
) {

    // Lazy initialization for the WearSyncManager
    @SuppressLint("StaticFieldLeak")
    private lateinit var wearSyncManager: WearSyncManager

    // The TileProcessHolder is responsible for handling the intents for the tile
    private val processHolder: TileProcessHolder by lazy {
        TileProcessHolder(wearSyncManager)
    }

    // Navigator to handle navigation actions
    override lateinit var navigator: MVINavigator

    // Initialize the WearSyncManager with the given context
    fun initialize(context: Context) {
        wearSyncManager = WearSyncManager(context)
    }

    init {
        Timber.d("TileViewModel initialized")
        // Send an initial intent to fetch smoke data
        intents().trySend(TileIntent.FetchSmokes)
    }

    // Transformer function to process the TileIntent and return the corresponding TileResult
    override suspend fun transformer(intent: TileIntent): Flow<TileResult> =
        processHolder.processIntent(intent)

    // Reducer function to update the state based on the TileResult
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

        is TileResult.AddSmokeSuccess -> previous.copy()

        is TileResult.Error -> previous.copy(error = result)
    }
}
