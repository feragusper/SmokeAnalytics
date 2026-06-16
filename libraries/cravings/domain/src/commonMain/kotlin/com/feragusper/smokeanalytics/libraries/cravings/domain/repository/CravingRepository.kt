package com.feragusper.smokeanalytics.libraries.cravings.domain.repository

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import kotlinx.datetime.Instant

/**
 * Stores cravings (urges the user tracked) in the cloud, mirroring the smoke
 * repository so the data survives reinstall and syncs across devices.
 */
interface CravingRepository {

    /**
     * Records a new craving.
     *
     * @param createdAt When the urge was felt.
     * @param targetAt When the user is allowed to smoke again, or null if no wait.
     * @return The created craving (with its server-assigned id).
     */
    suspend fun addCraving(createdAt: Instant, targetAt: Instant? = null): Craving

    /**
     * Fetches cravings, most recent first.
     *
     * @param start Inclusive lower bound on [Craving.createdAt], or null.
     * @param end Exclusive upper bound on [Craving.createdAt], or null.
     */
    suspend fun fetchCravings(
        start: Instant? = null,
        end: Instant? = null,
    ): List<Craving>

    /**
     * Returns the single pending craving, if any. There is at most one active
     * craving at a time.
     */
    suspend fun fetchActiveCraving(): Craving?

    /**
     * Resolves a craving with its final outcome and the points earned.
     */
    suspend fun resolveCraving(
        id: String,
        outcome: CravingOutcome,
        resolvedAt: Instant,
        pointsAwarded: Int,
    )

    /**
     * Deletes a craving.
     */
    suspend fun deleteCraving(id: String)
}
