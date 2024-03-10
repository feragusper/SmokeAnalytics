package com.feragusper.smokeanalytics.libraries.smokes.domain

import java.util.Date
import javax.inject.Inject

class AddSmokeUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
    suspend operator fun invoke(date: Date = Date()) = smokeRepository.addSmoke(date)
}
