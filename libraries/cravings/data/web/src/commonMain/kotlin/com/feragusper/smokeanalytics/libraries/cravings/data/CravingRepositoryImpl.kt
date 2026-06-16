package com.feragusper.smokeanalytics.libraries.cravings.data

import com.feragusper.smokeanalytics.libraries.cravings.domain.model.Craving
import com.feragusper.smokeanalytics.libraries.cravings.domain.model.CravingOutcome
import com.feragusper.smokeanalytics.libraries.cravings.domain.repository.CravingRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Query
import dev.gitlive.firebase.firestore.firestore
import kotlinx.datetime.Instant

class CravingRepositoryImpl(
    private val firebaseFirestore: FirebaseFirestore = Firebase.firestore,
    private val firebaseAuth: FirebaseAuth = Firebase.auth,
) : CravingRepository {

    interface FirestoreCollection {
        companion object {
            const val USERS = "users"
            const val CRAVINGS = "cravings"
        }
    }

    override suspend fun addCraving(createdAt: Instant, targetAt: Instant?): Craving {
        val reference = cravingsCollection().add(
            CravingEntity(
                createdAtMillis = createdAt.toEpochMilliseconds().toDouble(),
                targetAtMillis = targetAt?.toEpochMilliseconds()?.toDouble(),
                resolvedAtMillis = null,
                outcome = CravingOutcome.PENDING.name,
                pointsAwarded = 0.0,
            )
        )
        return Craving(
            id = reference.id,
            createdAt = createdAt,
            targetAt = targetAt,
            outcome = CravingOutcome.PENDING,
        )
    }

    override suspend fun fetchCravings(start: Instant?, end: Instant?): List<Craving> {
        var query: Query = cravingsCollection()
            .orderBy(CravingEntity.Fields.CREATED_AT_MILLIS, Direction.DESCENDING)
        if (start != null) {
            val startMillis = start.toEpochMilliseconds().toDouble()
            query = query.where { CravingEntity.Fields.CREATED_AT_MILLIS greaterThanOrEqualTo startMillis }
        }
        if (end != null) {
            val endMillis = end.toEpochMilliseconds().toDouble()
            query = query.where { CravingEntity.Fields.CREATED_AT_MILLIS lessThan endMillis }
        }
        return query.get().documents.mapNotNull { it.toCraving() }
    }

    override suspend fun fetchActiveCraving(): Craving? =
        // Only filter by outcome (no orderBy) so Firestore doesn't require a composite
        // index. There is at most one pending craving; pick the most recent client-side.
        cravingsCollection()
            .where { CravingEntity.Fields.OUTCOME equalTo CravingOutcome.PENDING.name }
            .get()
            .documents
            .mapNotNull { it.toCraving() }
            .maxByOrNull { it.createdAt }

    override suspend fun resolveCraving(
        id: String,
        outcome: CravingOutcome,
        resolvedAt: Instant,
        pointsAwarded: Int,
    ) {
        val existing = cravingsCollection().document(id).get().toEntity() ?: return
        cravingsCollection().document(id).set(
            existing.copy(
                outcome = outcome.name,
                resolvedAtMillis = resolvedAt.toEpochMilliseconds().toDouble(),
                pointsAwarded = pointsAwarded.toDouble(),
            )
        )
    }

    override suspend fun deleteCraving(id: String) {
        cravingsCollection().document(id).delete()
    }

    private fun cravingsCollection() = firebaseAuth.currentUser?.uid?.let { uid ->
        firebaseFirestore.collection("${FirestoreCollection.USERS}/$uid/${FirestoreCollection.CRAVINGS}")
    } ?: throw IllegalStateException("User not logged in")

    private fun DocumentSnapshot.toEntity(): CravingEntity? {
        val createdMillis = getOrNull<Double>(CravingEntity.Fields.CREATED_AT_MILLIS) ?: return null
        return CravingEntity(
            createdAtMillis = createdMillis,
            targetAtMillis = getOrNull<Double>(CravingEntity.Fields.TARGET_AT_MILLIS),
            resolvedAtMillis = getOrNull<Double>(CravingEntity.Fields.RESOLVED_AT_MILLIS),
            outcome = getOrNull<String>(CravingEntity.Fields.OUTCOME) ?: CravingOutcome.PENDING.name,
            pointsAwarded = getOrNull<Double>(CravingEntity.Fields.POINTS_AWARDED) ?: 0.0,
        )
    }

    private fun DocumentSnapshot.toCraving(): Craving? {
        val entity = toEntity() ?: return null
        return Craving(
            id = id,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAtMillis.toLong()),
            targetAt = entity.targetAtMillis?.let { Instant.fromEpochMilliseconds(it.toLong()) },
            resolvedAt = entity.resolvedAtMillis?.let { Instant.fromEpochMilliseconds(it.toLong()) },
            outcome = CravingOutcome.entries.firstOrNull { it.name == entity.outcome } ?: CravingOutcome.PENDING,
            pointsAwarded = entity.pointsAwarded.toInt(),
        )
    }

    private inline fun <reified T> DocumentSnapshot.getOrNull(field: String): T? =
        try {
            get(field)
        } catch (_: Throwable) {
            null
        }
}
