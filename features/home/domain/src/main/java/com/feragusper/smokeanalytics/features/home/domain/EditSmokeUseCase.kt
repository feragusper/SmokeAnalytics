package com.feragusper.smokeanalytics.features.home.domain

import java.util.Date
import javax.inject.Inject

class EditSmokeUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
    suspend operator fun invoke(id: String, date: Date) = smokeRepository.editSmoke(id, date)
}
