package com.feragusper.smokeanalytics.libraries.cravings.data

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant

class CravingRepositoryImpl constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : CravingRepository {

    override suspend fun addCraving(createdAt: Instant, targetAt: Instant?): Craving {
        val document = cravingsCollection().document()
        document.set(
            cravingPayload(
                createdAt = createdAt,
                targetAt = targetAt,
                resolvedAt = null,
                outcome = CravingOutcome.PENDING,
                pointsAwarded = 0,
            )
        ).await()
        return Craving(
            id = document.id,
            createdAt = createdAt,
            targetAt = targetAt,
            outcome = CravingOutcome.PENDING,
        )
    }

    override suspend fun fetchCravings(start: Instant?, end: Instant?): List<Craving> {
        var query = cravingsCollection()
            .orderBy(CravingEntity.Fields.CREATED_AT_MILLIS, Direction.DESCENDING)
        if (start != null) {
            query = query.whereGreaterThanOrEqualTo(
                CravingEntity.Fields.CREATED_AT_MILLIS,
                start.toEpochMilliseconds().toDouble(),
            )
        }
        if (end != null) {
            query = query.whereLessThan(
                CravingEntity.Fields.CREATED_AT_MILLIS,
                end.toEpochMilliseconds().toDouble(),
            )
        }
        return query.get().await().documents.mapNotNull { it.toCraving() }
    }

    override suspend fun fetchActiveCraving(): Craving? =
        cravingsCollection()
            .whereEqualTo(CravingEntity.Fields.OUTCOME, CravingOutcome.PENDING.name)
            .orderBy(CravingEntity.Fields.CREATED_AT_MILLIS, Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toCraving()

    override suspend fun resolveCraving(
        id: String,
        outcome: CravingOutcome,
        resolvedAt: Instant,
        pointsAwarded: Int,
    ) {
        cravingsCollection()
            .document(id)
            .update(
                mapOf(
                    CravingEntity.Fields.OUTCOME to outcome.name,
                    CravingEntity.Fields.RESOLVED_AT_MILLIS to resolvedAt.toEpochMilliseconds().toDouble(),
                    CravingEntity.Fields.POINTS_AWARDED to pointsAwarded.toDouble(),
                )
            )
            .await()
    }

    override suspend fun deleteCraving(id: String) {
        cravingsCollection().document(id).delete().await()
    }

    private fun cravingsCollection() = firebaseAuth.currentUser?.uid?.let { uid ->
        firebaseFirestore.collection("$USERS/$uid/$CRAVINGS")
    } ?: throw IllegalStateException("User not logged in")

    private fun cravingPayload(
        createdAt: Instant,
        targetAt: Instant?,
        resolvedAt: Instant?,
        outcome: CravingOutcome,
        pointsAwarded: Int,
    ): Map<String, Any?> = mapOf(
        CravingEntity.Fields.CREATED_AT_MILLIS to createdAt.toEpochMilliseconds().toDouble(),
        CravingEntity.Fields.TARGET_AT_MILLIS to targetAt?.toEpochMilliseconds()?.toDouble(),
        CravingEntity.Fields.RESOLVED_AT_MILLIS to resolvedAt?.toEpochMilliseconds()?.toDouble(),
        CravingEntity.Fields.OUTCOME to outcome.name,
        CravingEntity.Fields.POINTS_AWARDED to pointsAwarded.toDouble(),
    )

    private fun DocumentSnapshot.toCraving(): Craving? {
        val createdMillis = getDouble(CravingEntity.Fields.CREATED_AT_MILLIS) ?: return null
        return Craving(
            id = id,
            createdAt = Instant.fromEpochMilliseconds(createdMillis.toLong()),
            targetAt = getDouble(CravingEntity.Fields.TARGET_AT_MILLIS)
                ?.let { Instant.fromEpochMilliseconds(it.toLong()) },
            resolvedAt = getDouble(CravingEntity.Fields.RESOLVED_AT_MILLIS)
                ?.let { Instant.fromEpochMilliseconds(it.toLong()) },
            outcome = getString(CravingEntity.Fields.OUTCOME).toCravingOutcome(),
            pointsAwarded = getDouble(CravingEntity.Fields.POINTS_AWARDED)?.toInt() ?: 0,
        )
    }

    private fun String?.toCravingOutcome(): CravingOutcome =
        CravingOutcome.entries.firstOrNull { it.name == this } ?: CravingOutcome.PENDING

    private companion object {
        const val USERS = "users"
        const val CRAVINGS = "cravings"
    }
}
