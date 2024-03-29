package com.feragusper.smokeanalytics.libraries.smokes.domain

import javax.inject.Inject

/**
 * Use case for deleting an existing smoke event from the system. This encapsulates the logic for
 * removing a smoke event by its unique identifier.
 *
 * @property smokeRepository The [SmokeRepository] used for deleting the smoke event.
 */
class DeleteSmokeUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {

    /**
     * Invokes the use case to delete a smoke event by its ID.
     *
     * @param id The unique identifier of the smoke event to be deleted.
     */
    suspend operator fun invoke(id: String) = smokeRepository.deleteSmoke(id)
}
