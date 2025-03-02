package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.feragusper.smokeanalytics.libraries.wear.domain.WearSyncManager
import javax.inject.Inject

/**
 * Use case for deleting an existing smoke event from the system. This encapsulates the logic for
 * removing a smoke event by its unique identifier.
 *
 * @property smokeRepository The [SmokeRepository] used for deleting the smoke event.
 * @property wearSyncManager The [WearSyncManager] used for synchronizing data with the wear device.
 */
class DeleteSmokeUseCase @Inject constructor(
    private val smokeRepository: SmokeRepository,
    private val wearSyncManager: WearSyncManager.Mobile
) {

    /**
     * Invokes the use case to delete a smoke event by its ID.
     *
     * @param id The unique identifier of the smoke event to be deleted.
     */
    suspend operator fun invoke(id: String) {
        smokeRepository.deleteSmoke(id)
        wearSyncManager.syncWithWear()
    }
}
