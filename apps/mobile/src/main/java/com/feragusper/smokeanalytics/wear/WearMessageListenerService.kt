package com.feragusper.smokeanalytics.wear

import com.feragusper.smokeanalytics.libraries.architecture.common.coroutines.DispatcherProvider
import com.feragusper.smokeanalytics.libraries.wear.domain.WearSyncManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WearMessageListenerService : WearableListenerService() {

    @Inject
    lateinit var wearSyncManager: WearSyncManager.Mobile

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    private val serviceJob = SupervisorJob()

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        CoroutineScope(serviceJob + dispatcherProvider.io()).launch {
            runCatching { wearSyncManager.handleWearRequest(messageEvent.path) }
                .onFailure { Timber.w(it, "Failed to handle Wear request: ${messageEvent.path}") }
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
}
