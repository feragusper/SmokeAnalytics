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
        TileIntent.FetchSmokes ->
            listenForSmokeUpdates()
                .onEach { newSmokeCount ->
                    Timber.d("Received new smoke count: $newSmokeCount")
                }
                .catchAndLog {
                    Timber.e(it, "Error fetching smoke count.")
                    emit(TileResult.Error)
                }
    }

    /**
     * Creates a Flow to listen for data updates via WearSyncManager.
     */
    private fun listenForSmokeUpdates(): Flow<TileResult> = callbackFlow {
//        try {
//            Timber.d( "Listening for data updates.")
//            val initialSmokeCount = wearSyncManager.getSmokeCount()
//            trySend(TileResult.FetchSmokesSuccess(initialSmokeCount))
//        } catch (e: Exception) {
//            trySend(TileResult.Error)
//        }

        Timber.d("Fetching smoke count.")
        Timber.d("Sending request to mobile.")
        wearSyncManager.sendRequestToMobile()

        // Listen for updates and emit each new value
        wearSyncManager.listenForDataUpdates { newSmokeCount ->
            Timber.d("Received new smoke count: $newSmokeCount")
            trySend(TileResult.FetchSmokesSuccess(newSmokeCount))
        }

        // Close the callbackFlow when cancelled
        awaitClose { Timber.d("Stopped listening for data updates.") }
    }
}