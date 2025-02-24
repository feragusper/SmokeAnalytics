package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for editing the details of an existing smoke event. This allows for updating the timestamp
 * of a smoke event post-creation.
 *
 * @property smokeRepository The [SmokeRepository] used for editing the smoke event.
 */
class EditSmokeUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {

    /**
     * Invokes the use case to edit a smoke event's date and time.
     *
     * @param id The unique identifier of the smoke event to be edited.
     * @param date The new [LocalDateTime] for the smoke event.
     */
    suspend operator fun invoke(id: String, date: LocalDateTime) =
        smokeRepository.editSmoke(id, date)
}
