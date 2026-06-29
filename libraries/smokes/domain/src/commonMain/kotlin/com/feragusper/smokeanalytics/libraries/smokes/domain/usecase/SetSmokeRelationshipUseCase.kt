package com.feragusper.smokeanalytics.libraries.smokes.domain.usecase

import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeRelationship
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository

/**
 * Sets what a smoke was related to — a set of triggers (optionally with an "Other"
 * note), or [SmokeRelationship.Skipped] when the user declares it had no trigger.
 */
class SetSmokeRelationshipUseCase(
    private val smokeRepository: SmokeRepository,
) {

    suspend operator fun invoke(id: String, relationship: SmokeRelationship) {
        smokeRepository.setSmokeRelationship(id, relationship)
    }
}
