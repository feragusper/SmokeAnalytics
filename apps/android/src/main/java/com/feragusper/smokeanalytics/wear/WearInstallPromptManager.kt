package com.feragusper.smokeanalytics.wear

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class WearInstallPromptManager(private val context: Context) {

    private val nodeClient = Wearable.getNodeClient(context)
    private val capabilityClient = Wearable.getCapabilityClient(context)

    suspend fun findConnectedNodesWithoutApp(): List<Node> {
        return try {
            val connectedNodes = nodeClient.connectedNodes.await()
            if (connectedNodes.isEmpty()) return emptyList()

            val nodesWithApp = capabilityClient
                .getCapability(CAPABILITY_WEAR_APP, CapabilityClient.FILTER_REACHABLE)
                .await()
                .nodes

            connectedNodes.filter { node -> nodesWithApp.none { it.id == node.id } }
        } catch (e: Exception) {
            Timber.w(e, "Failed to query Wear OS nodes")
            emptyList()
        }
    }

    fun openPlayStoreOnWatch(nodeId: String) {
        try {
            val remoteActivityHelper = RemoteActivityHelper(context)
            remoteActivityHelper.startRemoteActivity(
                Intent(Intent.ACTION_VIEW)
                    .setData("market://details?id=${context.packageName}".toUri())
                    .addCategory(Intent.CATEGORY_BROWSABLE),
                nodeId
            )
        } catch (e: Exception) {
            Timber.w(e, "Failed to open Play Store on watch")
        }
    }

    companion object {
        private const val CAPABILITY_WEAR_APP = "smoke_analytics_wear_app"
    }
}
