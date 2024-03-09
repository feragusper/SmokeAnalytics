package com.feragusper.smokeanalytics.libraries.smokes.domain

import javax.inject.Inject

class DeleteSmokeUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
    suspend operator fun invoke(id: String) = smokeRepository.deleteSmoke(id)
}
