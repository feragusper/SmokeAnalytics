package com.feragusper.smokeanalytics.libraries.wear.data

import android.content.Context
import androidx.core.net.toUri
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.LAST_SMOKE_TIMESTAMP
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.SMOKE_COUNT_MONTH
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.SMOKE_COUNT_TODAY
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.SMOKE_COUNT_WEEK
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.SMOKE_DATA
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WearSyncManager(private val context: Context) {

    private val dataClient: DataClient by lazy {
        Wearable.getDataClient(context)
    }
    private val messageClient: MessageClient by lazy {
        Wearable.getMessageClient(context)
    }

    suspend fun sendRequestToMobile(path: String) {
        Timber.d("sendRequestToMobile")
        val nodes = getConnectedNodes()
        nodes.forEach { nodeId ->
            Timber.d("sendRequestToMobile: sending request to node $nodeId")
            sendMessageToNode(nodeId, path)
        }
    }

    fun listenForDataUpdates(onDataReceived: (smokesToday: Int, smokesPerWeek: Int, smokesPerMonth: Int, lastSmokeTimestamp: Long?) -> Unit) {
        Timber.d("listenForDataUpdates")
        val dataChangedListener = DataClient.OnDataChangedListener { dataEvents ->
            for (event in dataEvents) {
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val dataItem = event.dataItem
                    if (dataItem.uri.path == SMOKE_DATA) {
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
        dataClient.addListener(dataChangedListener)
    }

    private suspend fun getConnectedNodes(): List<String> = withContext(Dispatchers.IO) {
        Timber.d("getConnectedNodes")
        suspendCancellableCoroutine { continuation ->
            Wearable.getNodeClient(context).connectedNodes
                .addOnSuccessListener { nodes ->
                    continuation.resume(nodes.map { it.id })
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    private suspend fun sendMessageToNode(nodeId: String, path: String) =
        withContext(Dispatchers.IO) {
            Timber.d("sendMessageToNode")
            suspendCancellableCoroutine { continuation ->
                messageClient.sendMessage(nodeId, path, null)
                    .addOnSuccessListener {
                        Timber.d("sendMessageToNode: message sent successfully")
                        continuation.resume(Unit)
                    }
                    .addOnFailureListener { e ->
                        Timber.d("sendMessageToNode: error", e)
                        continuation.resumeWithException(e)
                    }
            }
        }

    /**
     * Sends smoke count data to the wearable app.
     */
    fun sendSmokeData(smokeCount: Int) {
        Timber.d("sendSmokeData")
        val putDataMapRequest = PutDataMapRequest.create(SMOKE_DATA).apply {
            dataMap.putInt(SMOKE_COUNT_TODAY, smokeCount)
        }

        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()

        dataClient.putDataItem(putDataRequest)
            .addOnSuccessListener {
                println("Data sent to Wear successfully!")
            }
            .addOnFailureListener { e ->
                println("Failed to send data to Wear: ${e.message}")
            }
    }

    /**
     * Retrieves the latest smoke count synchronously.
     */
    suspend fun getSmokeCount(): Int = withContext(Dispatchers.IO) {
        Timber.d("getSmokeCount")
        suspendCancellableCoroutine { continuation ->
            val uri = "wear://*/$SMOKE_DATA".toUri()
            dataClient.getDataItems(uri).addOnSuccessListener { dataItemBuffer ->
                Timber.d("getSmokeCount: dataItemBuffer count: ${dataItemBuffer.count}")
                var smokeCount = 0
                for (dataItem in dataItemBuffer) {
                    Timber.d("getSmokeCount: dataItem.uri.path: ${dataItem.uri.path}")
                    if (dataItem.uri.path == SMOKE_DATA) {
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                        smokeCount = dataMap.getInt("smoke_count", 0)
                        Timber.d("getSmokeCount: smoke_count = $smokeCount")
                    } else {
                        Timber.d("getSmokeCount: path does not match")
                    }
                }
                Timber.d("getSmokeCount: final smoke count = $smokeCount")
                dataItemBuffer.release()
                continuation.resume(smokeCount)
            }.addOnFailureListener { e ->
                Timber.d("getSmokeCount: error", e)
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Sends a short message to the wearable device.
     */
    @Suppress("unused")
    fun sendMessageToWear(nodeId: String, message: String) {
        val path = "/message_path"
        messageClient.sendMessage(nodeId, path, message.toByteArray())
            .addOnSuccessListener {
                println("Message sent successfully to Wear!")
            }
            .addOnFailureListener { e ->
                println("Failed to send message to Wear: ${e.message}")
            }
    }

}