package com.feragusper.smokeanalytics.libraries.wear.data

import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.SMOKE_COUNT
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MobileMessageService : WearableListenerService(), CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.IO + job

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Timber.d("onMessageReceived: ${messageEvent.path}")
        if (messageEvent.path == WearPaths.REQUEST_SMOKES) {
            launch {
//                val currentSmokeCount = getCurrentSmokeCountFromLocalStorage()
//                respondToWearWithSmokeCount(currentSmokeCount)
            }
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        super.onDataChanged(p0)
        Timber.d("onDataChanged")
        val putDataMapRequest = PutDataMapRequest.create(WearPaths.SMOKE_DATA).apply {
            Timber.d("onDataChanged: fetchSmokes")
            launch {
                Timber.d("onDataChanged: fetchSmokes: launch")
//                dataMap.putInt(SMOKE_COUNT, smokeRepository.fetchSmokes().size)
            }
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        Wearable.getDataClient(this).putDataItem(putDataRequest)
    }

//    private suspend fun getCurrentSmokeCountFromLocalStorage(): Int {
//        Timber.d("getCurrentSmokeCountFromLocalStorage")
////        return smokeRepository.fetchSmokes().size
//    }

    private fun respondToWearWithSmokeCount(smokeCount: Int) {
        Timber.d("respondToWearWithSmokeCount: $smokeCount")
        val putDataMapRequest = PutDataMapRequest.create(WearPaths.SMOKE_DATA).apply {
            dataMap.putInt(SMOKE_COUNT, smokeCount)
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        Wearable.getDataClient(this).putDataItem(putDataRequest)
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
        job.cancel()
    }
}