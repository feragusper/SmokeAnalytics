package com.feragusper.smokeanalytics.libraries.smokes.domain

import javax.inject.Inject

class FetchSmokesUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
    suspend operator fun invoke() = smokeRepository.fetchSmokes()
}
