package com.feragusper.smokeanalytics.libraries.wear.data

import android.content.Context
import com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.DispatcherProvider
import com.feragusper.smokeanalytics.libraries.architecture.domain.utcMillis
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.wear.domain.WearSyncManager
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import timber.log.Timber
import java.io.Closeable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementation of [WearSyncManager] that handles synchronization between the mobile app
 * and a Wear OS device.
 *
 * @param context The application context.
 */
class WearSyncManagerImpl(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider,
) {

    /**
     * Handles communication from the mobile app to the Wear OS device.
     *
     * @param smokeRepository The repository to fetch smoke data.
     */
    inner class Mobile(
        private val smokeRepository: SmokeRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : WearSyncManager.Mobile {

        /**
         * Synchronizes the smoke count data with the connected Wear OS device.
         */
        override suspend fun syncWithWear() {
            val preferences = userPreferencesRepository.fetch()
            val smokeCount = smokeRepository.fetchSmokeCount(
                dayStartHour = preferences.dayStartHour,
                manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
            )
            respondToWearWithSmokeCount(
                smokeCount = smokeCount,
                targetGapMinutes = smokeCount.targetGapMinutes(preferences.awakeMinutesPerDay),
                averageSmokesPerDayWeek = smokeCount.averageSmokesPerDayWeek(),
            )
        }

        override suspend fun handleWearRequest(path: String) {
            when (path) {
                WearPaths.REQUEST_SMOKES -> syncWithWear()
                WearPaths.ADD_SMOKE -> {
                    smokeRepository.addSmoke(Clock.System.now())
                    syncWithWear()
                }
                else -> Timber.w("Ignoring unknown Wear request: $path")
            }
        }

        /**
         * Sends the smoke count data to the Wear OS device using DataClient.
         *
         * @param smokeCount The [SmokeCount] object containing today's, week's, and month's smoke data.
         */
        private fun respondToWearWithSmokeCount(
            smokeCount: SmokeCount,
            targetGapMinutes: Int,
            averageSmokesPerDayWeek: Double,
        ) {
            val putDataMapRequest = PutDataMapRequest.create(WearPaths.SMOKE_DATA).apply {
                dataMap.putInt(WearPaths.SMOKE_COUNT_TODAY, smokeCount.today.size)
                dataMap.putInt(WearPaths.TARGET_GAP_MINUTES, targetGapMinutes)
                dataMap.putDouble(WearPaths.AVERAGE_SMOKES_PER_DAY_WEEK, averageSmokesPerDayWeek)
                smokeCount.lastSmoke?.date?.utcMillis()?.let {
                    dataMap.putLong(WearPaths.LAST_SMOKE_TIMESTAMP, it)
                }
            }
            val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
            dataClient.putDataItem(putDataRequest)
        }
    }

    /**
     * Handles communication from the Wear OS device to the mobile app.
     */
    inner class Wear : WearSyncManager.Wear {

        /**
         * Sends a request to the mobile app via the connected wearable device.
         *
         * @param path The communication path to the mobile app.
         */
        override suspend fun sendRequestToMobile(path: String) {
            Timber.d("sendRequestToMobile")
            val nodes = getConnectedNodes()
            nodes.forEach { nodeId ->
                Timber.d("sendRequestToMobile: sending request to node $nodeId")
                sendMessageToNode(nodeId, path)
            }
        }

        /**
         * Listens for data updates from the wearable device and provides them to the callback.
         *
         * @param onDataReceived Callback function to handle the received data.
         */
        override fun listenForDataUpdates(
            onDataReceived: (
                todayCount: Int,
                targetGapMinutes: Int,
                averageSmokesPerDayWeek: Double,
                lastSmokeTimestamp: Long?,
            ) -> Unit
        ): Closeable {
            Timber.d("listenForDataUpdates")

            fun dispatchDataItem(dataItem: com.google.android.gms.wearable.DataItem) {
                if (dataItem.uri.path != WearPaths.SMOKE_DATA) return

                val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                onDataReceived(
                    dataMap.getInt(WearPaths.SMOKE_COUNT_TODAY),
                    dataMap.getInt(WearPaths.TARGET_GAP_MINUTES),
                    dataMap.getDouble(WearPaths.AVERAGE_SMOKES_PER_DAY_WEEK),
                    dataMap.takeIf { it.containsKey(WearPaths.LAST_SMOKE_TIMESTAMP) }
                        ?.getLong(WearPaths.LAST_SMOKE_TIMESTAMP),
                )
            }

            val dataChangedListener = DataClient.OnDataChangedListener { dataEvents ->
                for (event in dataEvents) {
                    if (event.type == DataEvent.TYPE_CHANGED) {
                        dispatchDataItem(event.dataItem)
                    }
                }
            }
            dataClient.addListener(dataChangedListener)

            dataClient.dataItems
                .addOnSuccessListener { dataItems ->
                    dataItems.forEach(::dispatchDataItem)
                    dataItems.release()
                }
                .addOnFailureListener { error ->
                    Timber.w(error, "Could not read latest Wear data item")
                }

            return Closeable {
                dataClient.removeListener(dataChangedListener)
            }
        }

        /**
         * Retrieves a list of connected nodes (wearable devices).
         *
         * @return List of connected node IDs.
         */
        private suspend fun getConnectedNodes(): List<String> =
            withContext(dispatcherProvider.io()) {
                Timber.d("getConnectedNodes")
                suspendCancellableCoroutine { continuation ->
                    Wearable.getNodeClient(context).connectedNodes
                        .addOnSuccessListener { nodes -> continuation.resume(nodes.map { it.id }) }
                        .addOnFailureListener { e -> continuation.resumeWithException(e) }
                }
            }

        /**
         * Sends a message to a specific node.
         *
         * @param nodeId The ID of the node (wearable device) to send the message to.
         * @param path The communication path for the message.
         */
        private suspend fun sendMessageToNode(nodeId: String, path: String) =
            withContext(dispatcherProvider.io()) {
                Timber.d("sendMessageToNode")
                suspendCancellableCoroutine { continuation ->
                    messageClient.sendMessage(nodeId, path, null)
                        .addOnSuccessListener {
                            Timber.d("sendMessageToNode: message sent successfully")
                            continuation.resume(Unit)
                        }
                        .addOnFailureListener { e ->
                            Timber.d(e, "sendMessageToNode: error")
                            continuation.resumeWithException(e)
                        }
                }
            }
    }

    // Lazy initialization of DataClient and MessageClient for Wearable communication
    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }
}

private fun SmokeCount.targetGapMinutes(awakeMinutesPerDay: Int): Int = when (val count = today.size) {
    0 -> awakeMinutesPerDay
    else -> (awakeMinutesPerDay / count).coerceAtLeast(1)
}

private fun SmokeCount.averageSmokesPerDayWeek(): Double = week / 7.0
