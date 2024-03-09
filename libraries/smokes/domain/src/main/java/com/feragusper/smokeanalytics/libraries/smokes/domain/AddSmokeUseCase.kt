package com.feragusper.smokeanalytics.libraries.smokes.domain

import javax.inject.Inject

class AddSmokeUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
    suspend operator fun invoke() = smokeRepository.addSmoke()
}
