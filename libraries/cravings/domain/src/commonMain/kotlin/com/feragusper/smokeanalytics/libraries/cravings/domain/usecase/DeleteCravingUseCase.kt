package com.feragusper.smokeanalytics.libraries.cravings.domain.usecase

import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository

/**
 * Deletes a craving outright, without recording an outcome or awarding points.
 *
 * Used to undo a craving that was tracked by mistake (e.g. the user tapped the
 * button by accident) so it leaves no trace in the stats.
 */
class DeleteCravingUseCase(
    private val cravingRepository: CravingRepository,
) {

    suspend operator fun invoke(id: String) = cravingRepository.deleteCraving(id)
}
