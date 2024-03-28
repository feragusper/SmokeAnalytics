package com.feragusper.smokeanalytics.libraries.smokes.domain

import java.time.LocalDateTime
import javax.inject.Inject

data class FetchSmokesUseCase @Inject constructor(private val smokeRepository: SmokeRepository) {
    suspend operator fun invoke(date: LocalDateTime? = null) = smokeRepository.fetchSmokes(date)
}
