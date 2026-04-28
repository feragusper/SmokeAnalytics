package com.feragusper.smokeanalytics.tile

import com.feragusper.smokeanalytics.libraries.architecture.presentation.extensions.catchAndLog
import com.feragusper.smokeanalytics.libraries.architecture.presentation.process.MVIProcessHolder
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths
import com.feragusper.smokeanalytics.libraries.wear.data.WearSyncManagerImpl
import com.feragusper.smokeanalytics.libraries.wear.domain.WearSyncManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * This class handles the processing of intents related to the Wear OS Tile.
 * It listens for updates, such as fetching smoke counts or adding smoke, and communicates
 * with the mobile app via the WearSyncManager.
 */
class TileProcessHolder @Inject constructor(
    private val wearSyncManager: WearSyncManager.Wear
) : MVIProcessHolder<TileIntent, TileResult> {

    /**
     * Processes the given [TileIntent] and returns a Flow of [TileResult].
     */
    override fun processIntent(intent: TileIntent): Flow<TileResult> = when (intent) {
        // Fetches the current smoke count and listens for updates.
        TileIntent.FetchSmokes ->
            listenForSmokeUpdates()
                .onEach { newSmokeCount ->
                    Timber.d("Received new smoke count: $newSmokeCount")
                }
                .catchAndLog {
                    Timber.e(it, "Error fetching smoke count.")
                    emit(TileResult.Error)  // Emitting an error result in case of failure
                }

        TileIntent.RefreshSmokes -> callbackFlow {
            launch {
                try {
                    wearSyncManager.sendRequestToMobile(WearPaths.REQUEST_SMOKES)
                } catch (e: Exception) {
                    Timber.e(e, "Error requesting smoke count from mobile.")
                    trySend(TileResult.Error)
                } finally {
                    close()
                }
            }

            awaitClose { Timber.d("Closed refresh smoke action.") }
        }

        // Handles the "Add Smoke" action by sending a request to the mobile app.
        is TileIntent.AddSmoke -> callbackFlow {
            Timber.d("Adding smoke.")
            trySend(TileResult.AddSmokeStarted(intent.requestedAtMillis))
            // Launched within the callbackFlow to ensure it runs as a coroutine
            launch {
                try {
                    wearSyncManager.sendRequestToMobile(WearPaths.ADD_SMOKE)
                    // Emit success after sending the request
                    trySend(TileResult.AddSmokeRequestSent)
                } catch (e: Exception) {
                    Timber.e(e, "Error sending request to mobile.")
                    trySend(TileResult.Error)  // Emitting error if the request fails
                } finally {
                    close()
                }
            }
            awaitClose { Timber.d("Closed add smoke action.") }
        }
    }

    /**
     * Creates a Flow to listen for updates from the mobile app via [WearSyncManagerImpl].
     * This will be used to fetch the latest smoke counts and other related data.
     */
    private fun listenForSmokeUpdates(): Flow<TileResult> = callbackFlow {
        Timber.d("Fetching smoke count.")

        // Listen before requesting data so the first mobile response cannot be missed.
        val subscription = wearSyncManager.listenForDataUpdates { todayCount, targetGapMinutes, averageSmokesPerDayWeek, lastSmokeTimestamp ->
            trySend(
                TileResult.FetchSmokesSuccess(
                    todayCount = todayCount,
                    targetGapMinutes = targetGapMinutes,
                    averageSmokesPerDayWeek = averageSmokesPerDayWeek,
                    lastSmokeTimestamp = lastSmokeTimestamp,
                )
            )
        }

        launch {
            try {
                wearSyncManager.sendRequestToMobile(WearPaths.REQUEST_SMOKES)
            } catch (e: Exception) {
                Timber.e(e, "Error requesting smoke count from mobile.")
                trySend(TileResult.Error)
            }
        }

        awaitClose {
            Timber.d("Stopped listening for data updates.")
            subscription.close()
        }
    }
}
