package com.feragusper.smokeanalytics.libraries.smokes.domain

import java.time.LocalDateTime
import javax.inject.Inject

class EditSmokeUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
    suspend operator fun invoke(id: String, date: LocalDateTime) =
        smokeRepository.editSmoke(id, date)
}
