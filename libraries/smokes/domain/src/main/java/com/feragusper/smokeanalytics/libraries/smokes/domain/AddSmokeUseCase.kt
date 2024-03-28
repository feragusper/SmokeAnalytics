package com.feragusper.smokeanalytics.libraries.smokes.domain

import java.time.LocalDateTime
import javax.inject.Inject

class AddSmokeUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
    suspend operator fun invoke(date: LocalDateTime = LocalDateTime.now()) =
        smokeRepository.addSmoke(date)
}
