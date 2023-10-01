package com.feragusper.smokeanalytics.features.home.domain

import javax.inject.Inject

class AddSmokeUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
    suspend operator fun invoke() = smokeRepository.addSmoke()
}
