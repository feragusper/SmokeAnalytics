package com.feragusper.smokeanalytics.libraries.wear.data

import android.content.Context
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.LAST_SMOKE_TIMESTAMP
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.SMOKE_COUNT_MONTH
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.SMOKE_COUNT_TODAY
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.SMOKE_COUNT_WEEK
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.SMOKE_DATA
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WearSyncManager(private val context: Context) {

    // Lazy initialization of DataClient and MessageClient for Wearable communication
    private val dataClient: DataClient by lazy {
        Wearable.getDataClient(context)
    }
    private val messageClient: MessageClient by lazy {
        Wearable.getMessageClient(context)
    }

    /**
     * Sends a request to the mobile app via the connected wearable device.
     *
     * @param path The communication path to the mobile app.
     */
    suspend fun sendRequestToMobile(path: String) {
        Timber.d("sendRequestToMobile")
        val nodes = getConnectedNodes()  // Get all connected wearable nodes
        nodes.forEach { nodeId ->
            Timber.d("sendRequestToMobile: sending request to node $nodeId")
            sendMessageToNode(nodeId, path)  // Send message to each connected node
        }
    }

    /**
     * Listens for data updates from the wearable device and provides them to the callback.
     *
     * @param onDataReceived Callback function to handle the received data.
     */
    fun listenForDataUpdates(onDataReceived: (smokesToday: Int, smokesPerWeek: Int, smokesPerMonth: Int, lastSmokeTimestamp: Long?) -> Unit) {
        Timber.d("listenForDataUpdates")

        // Data change listener for monitoring smoke data updates
        val dataChangedListener = DataClient.OnDataChangedListener { dataEvents ->
            for (event in dataEvents) {
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val dataItem = event.dataItem
                    if (dataItem.uri.path == SMOKE_DATA) {
                        // Extract smoke data from the received DataItem
                        val smokesToday = DataMapItem.fromDataItem(dataItem)
                            .dataMap
                            .getInt(SMOKE_COUNT_TODAY)
                        val smokesPerWeek = DataMapItem.fromDataItem(dataItem)
                            .dataMap
                            .getInt(SMOKE_COUNT_WEEK)
                        val smokesPerMonth = DataMapItem.fromDataItem(dataItem)
                            .dataMap
                            .getInt(SMOKE_COUNT_MONTH)
                        val lastSmokeTimestamp = DataMapItem.fromDataItem(dataItem)
                            .dataMap
                            .getLong(LAST_SMOKE_TIMESTAMP)

                        // Call the callback with the extracted data
                        onDataReceived(
                            smokesToday,
                            smokesPerWeek,
                            smokesPerMonth,
                            lastSmokeTimestamp
                        )
                    }
                }
            }
        }

        // Register the listener to the DataClient
        dataClient.addListener(dataChangedListener)
    }

    /**
     * Retrieves a list of connected nodes (wearable devices).
     *
     * @return List of connected node IDs.
     */
    private suspend fun getConnectedNodes(): List<String> = withContext(Dispatchers.IO) {
        Timber.d("getConnectedNodes")
        suspendCancellableCoroutine { continuation ->
            Wearable.getNodeClient(context).connectedNodes
                .addOnSuccessListener { nodes ->
                    continuation.resume(nodes.map { it.id })  // Return node IDs of connected devices
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)  // In case of error, propagate the exception
                }
        }
    }

    /**
     * Sends a message to a specific node.
     *
     * @param nodeId The ID of the node (wearable device) to send the message to.
     * @param path The communication path for the message.
     */
    private suspend fun sendMessageToNode(nodeId: String, path: String) =
        withContext(Dispatchers.IO) {
            Timber.d("sendMessageToNode")
            suspendCancellableCoroutine { continuation ->
                messageClient.sendMessage(nodeId, path, null)
                    .addOnSuccessListener {
                        Timber.d("sendMessageToNode: message sent successfully")
                        continuation.resume(Unit)  // Successfully sent the message
                    }
                    .addOnFailureListener { e ->
                        Timber.d(e, "sendMessageToNode: error")
                        continuation.resumeWithException(e)  // Propagate error if message failed
                    }
            }
        }

    /**
     * Sends a short message to the wearable device.
     *
     * @param nodeId The ID of the node (wearable device) to send the message to.
     * @param message The message to send.
     */
    @Suppress("unused")
    fun sendMessageToWear(nodeId: String, message: String) {
        val path = "/message_path"
        messageClient.sendMessage(nodeId, path, message.toByteArray())
            .addOnSuccessListener {
                Timber.d("Message sent successfully to Wear!")  // Log success
            }
            .addOnFailureListener { e ->
                Timber.e("Failed to send message to Wear: ${e.message}")  // Log failure
            }
    }
}
