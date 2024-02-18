package com.feragusper.smokeanalytics.features.home.domain

import java.util.Date
import javax.inject.Inject

class DeleteSmokeUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
    suspend operator fun invoke(id: String) = smokeRepository.deleteSmoke(id)
}
