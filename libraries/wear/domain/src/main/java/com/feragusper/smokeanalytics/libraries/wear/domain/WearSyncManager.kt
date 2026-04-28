package com.feragusper.smokeanalytics.libraries.wear.domain

import java.io.Closeable

/**
 * Defines the contract for synchronizing smoke data between a mobile app and a Wear OS device.
 *
 * This sealed interface ensures that only `Mobile` and `Wear` implementations are allowed.
 */
sealed interface WearSyncManager {

    /**
     * Interface for the **mobile app side** of the synchronization.
     */
    interface Mobile : WearSyncManager {
        /**
         * Synchronizes smoke data with the connected Wear OS device.
         */
        suspend fun syncWithWear()

        /**
         * Handles a message sent by a Wear OS device.
         */
        suspend fun handleWearRequest(path: String)
    }

    /**
     * Interface for the **Wear OS device side** of the synchronization.
     */
    interface Wear : WearSyncManager {
        /**
         * Sends a request from the Wear OS device to the mobile app.
         *
         * @param path The communication path for the request.
         */
        suspend fun sendRequestToMobile(path: String)

        /**
         * Listens for data updates from the mobile app and processes them.
         *
         * @param onDataReceived Callback function that receives the pulse snapshot data.
         */
        fun listenForDataUpdates(
            onDataReceived: (
                todayCount: Int,
                targetGapMinutes: Int,
                averageSmokesPerDayWeek: Double,
                lastSmokeTimestamp: Long?,
            ) -> Unit
        ): Closeable
    }
}
