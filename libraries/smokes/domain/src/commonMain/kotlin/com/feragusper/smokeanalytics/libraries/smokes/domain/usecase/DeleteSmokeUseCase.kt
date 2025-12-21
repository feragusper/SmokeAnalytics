// commonMain
package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository

class DeleteSmokeUseCase(
    private val smokeRepository: SmokeRepository,
) {

    suspend operator fun invoke(id: String) {
        smokeRepository.deleteSmoke(id)
    }
}