package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.wear.domain.WearSyncManager
import javax.inject.Inject

/**
 * Use case for adding a new smoke event to the system. This operation encapsulates the business logic
 * for creating and storing a new smoke event.
 *
 * @property wearSyncManager The [WearSyncManager] used for synchronizing data with the wear device.
 */
class SyncWithWearUseCase @Inject constructor(
    private val wearSyncManager: WearSyncManager.Mobile,
) {

    /**
     * Invokes the use case to add a new smoke event.
     */
    suspend operator fun invoke() {
        wearSyncManager.syncWithWear()
    }
}
