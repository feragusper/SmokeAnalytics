package com.feragusper.smokeanalytics.libraries.wear.data

import android.content.Context
import android.net.Uri
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
    private val messageClient: MessageClient = Wearable.getMessageClient(context)

    /**
     * Sends smoke count data to the wearable app.
     */
    fun sendSmokeData(smokeCount: Int) {
        val path = "/smoke_data"
        val putDataMapRequest = PutDataMapRequest.create(path).apply {
            dataMap.putInt("smoke_count", smokeCount)
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
     * Listens for data updates coming from the wearable app.
     */
    fun listenForDataUpdates(onDataReceived: (Int) -> Unit) {
        val dataChangedListener = DataClient.OnDataChangedListener { dataEvents ->
            println("Data changed on Wear!")
            for (event in dataEvents) {
                println("Data event: $event")
                if (event.type == DataEvent.TYPE_CHANGED) {
                    println("Data event type changed")
                    val dataItem = event.dataItem
                    if (dataItem.uri.path == "/smoke_data") {
                        println("Data event path matches")
                        val smokeCount = DataMapItem.fromDataItem(dataItem)
                            .dataMap
                            .getInt("smoke_count")
                        onDataReceived(smokeCount)
                    }
                }
            }
        }
        dataClient.addListener(dataChangedListener)
    }

    /**
     * Retrieves the latest smoke count synchronously.
     */
    suspend fun getSmokeCount(): Int = withContext(Dispatchers.IO) {
        Timber.d("WearSyncManager", "getSmokeCount")
        suspendCancellableCoroutine { continuation ->
            val uri = Uri.parse("wear://*/smoke_data") // Usar una URI válida
            dataClient.getDataItems(uri).addOnSuccessListener { dataItemBuffer ->
                Timber.d("WearSyncManager", "getSmokeCount: dataItemBuffer count: ${dataItemBuffer.count}")
                var smokeCount = 0
                for (dataItem in dataItemBuffer) {
                    Timber.d("WearSyncManager", "getSmokeCount: dataItem.uri.path: ${dataItem.uri.path}")
                    if (dataItem.uri.path == "/smoke_data") {
                        val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                        smokeCount = dataMap.getInt("smoke_count", 0)
                        Timber.d("WearSyncManager", "getSmokeCount: smoke_count = $smokeCount")
                    } else {
                        Timber.d("WearSyncManager", "getSmokeCount: path does not match")
                    }
                }
                Timber.d("WearSyncManager", "getSmokeCount: final smoke count = $smokeCount")
                dataItemBuffer.release()
                continuation.resume(smokeCount)
            }.addOnFailureListener { e ->
                Timber.d("WearSyncManager", "getSmokeCount: error", e)
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

    /**
     * Retrieves connected nodes (Wear devices) for sending messages.
     */
    @Suppress("unused")
    suspend fun getConnectedNodes(): List<String> = withContext(Dispatchers.IO) {
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
}