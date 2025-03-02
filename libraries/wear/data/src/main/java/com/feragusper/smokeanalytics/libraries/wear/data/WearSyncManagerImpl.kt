package com.feragusper.smokeanalytics.libraries.wear.data

import android.content.Context
import com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.DispatcherProvider
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.utcMillis
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.wear.domain.WearSyncManager
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime
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
     * @param coroutineScope The coroutine scope to execute async operations.
     */
    inner class Mobile(
        private val smokeRepository: SmokeRepository,
        private val coroutineScope: CoroutineScope,
    ) : WearSyncManager.Mobile, MessageClient.OnMessageReceivedListener {

        /**
         * Initializes the WearSyncManager by registering the message listener.
         */
        init {
            Wearable.getMessageClient(context).addListener(this)
        }

        /**
         * Synchronizes the smoke count data with the connected Wear OS device.
         */
        override suspend fun syncWithWear() {
            respondToWearWithSmokeCount(smokeRepository.fetchSmokeCount())
        }

        /**
         * Handles incoming messages from Wear OS devices.
         *
         * @param messageEvent The received message event.
         */
        override fun onMessageReceived(messageEvent: MessageEvent) {
            Timber.d("onMessageReceived: ${messageEvent.path}")
            coroutineScope.launch(dispatcherProvider.io()) {
                when (messageEvent.path) {
                    WearPaths.REQUEST_SMOKES -> syncWithWear()
                    WearPaths.ADD_SMOKE -> smokeRepository.addSmoke(LocalDateTime.now())
                }
            }
        }

        /**
         * Sends the smoke count data to the Wear OS device using DataClient.
         *
         * @param smokeCount The [SmokeCount] object containing today's, week's, and month's smoke data.
         */
        private fun respondToWearWithSmokeCount(smokeCount: SmokeCount) {
            val putDataMapRequest = PutDataMapRequest.create(WearPaths.SMOKE_DATA).apply {
                dataMap.putInt(WearPaths.SMOKE_COUNT_TODAY, smokeCount.today.size)
                dataMap.putInt(WearPaths.SMOKE_COUNT_WEEK, smokeCount.week)
                dataMap.putInt(WearPaths.SMOKE_COUNT_MONTH, smokeCount.month)
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
            onDataReceived: (smokesToday: Int, smokesPerWeek: Int, smokesPerMonth: Int, lastSmokeTimestamp: Long?) -> Unit
        ) {
            Timber.d("listenForDataUpdates")

            val dataChangedListener = DataClient.OnDataChangedListener { dataEvents ->
                for (event in dataEvents) {
                    if (event.type == DataEvent.TYPE_CHANGED) {
                        val dataItem = event.dataItem
                        if (dataItem.uri.path == WearPaths.SMOKE_DATA) {
                            val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

                            onDataReceived(
                                dataMap.getInt(WearPaths.SMOKE_COUNT_TODAY),
                                dataMap.getInt(WearPaths.SMOKE_COUNT_WEEK),
                                dataMap.getInt(WearPaths.SMOKE_COUNT_MONTH),
                                dataMap.getLong(WearPaths.LAST_SMOKE_TIMESTAMP)
                            )
                        }
                    }
                }
            }
            dataClient.addListener(dataChangedListener)
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
