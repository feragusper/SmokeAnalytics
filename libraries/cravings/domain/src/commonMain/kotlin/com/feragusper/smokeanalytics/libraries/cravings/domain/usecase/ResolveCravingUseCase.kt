package com.feragusper.smokeanalytics.libraries.cravings.domain.usecase

import com.feragusper.smokeanalytics.libraries.cravings.domain.CravingReward
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository
import kotlinx.datetime.Instant
import kotlin.time.Clock

/**
 * Resolves a pending craving, computing the reward from how long the user waited.
 *
 * @return the points awarded.
 */
class ResolveCravingUseCase(
    private val cravingRepository: CravingRepository,
) {

    suspend operator fun invoke(
        craving: Craving,
        outcome: CravingOutcome,
        resolvedAt: Instant = Clock.System.now(),
    ): Int {
        val waitedMinutes = ((resolvedAt - craving.createdAt).inWholeMinutes)
            .coerceAtLeast(0)
            .toInt()
        val points = CravingReward.pointsFor(outcome, waitedMinutes)
        cravingRepository.resolveCraving(
            id = craving.id,
            outcome = outcome,
            resolvedAt = resolvedAt,
            pointsAwarded = points,
        )
        return points
    }
}
