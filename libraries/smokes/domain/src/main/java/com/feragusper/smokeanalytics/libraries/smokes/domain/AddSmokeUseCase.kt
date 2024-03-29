package com.feragusper.smokeanalytics.libraries.smokes.domain

import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for adding a new smoke event to the system. This operation encapsulates the business logic
 * for creating and storing a new smoke event.
 *
 * @property smokeRepository The [SmokeRepository] used for adding the smoke event.
 */
class AddSmokeUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {

    /**
     * Invokes the use case to add a new smoke event.
     *
     * @param date The [LocalDateTime] when the smoke event occurred, defaults to the current time.
     */
    suspend operator fun invoke(date: LocalDateTime = LocalDateTime.now()) =
        smokeRepository.addSmoke(date)
}
