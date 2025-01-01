package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.catchAndLog
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.wear.data.WearSyncManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class TileProcessHolder @Inject constructor(
    private val wearSyncManager: WearSyncManager
) : MVIProcessHolder<TileIntent, TileResult> {

    override fun processIntent(intent: TileIntent): Flow<TileResult> = when (intent) {
        TileIntent.FetchSmokes -> {
            listenForSmokeUpdates()
                .onEach { newSmokeCount ->
                    Timber.d("TileProcessHolder", "Received new smoke count: $newSmokeCount")
                }
                .catchAndLog {
                    emit(TileResult.Error)
                }
        }
    }

    /**
     * Creates a Flow to listen for data updates via WearSyncManager.
     */
    private fun listenForSmokeUpdates(): Flow<TileResult> = callbackFlow {
        // Emit the initial value from getSmokeCount()
        try {
            val initialSmokeCount = wearSyncManager.getSmokeCount()
            trySend(TileResult.FetchSmokesSuccess(initialSmokeCount))
        } catch (e: Exception) {
            trySend(TileResult.Error)
        }

        // Listen for updates and emit each new value
        wearSyncManager.listenForDataUpdates { newSmokeCount ->
            trySend(TileResult.FetchSmokesSuccess(newSmokeCount))
        }

        // Close the callbackFlow when cancelled
        awaitClose { Timber.d("TileProcessHolder", "Stopped listening for data updates.") }
    }
}