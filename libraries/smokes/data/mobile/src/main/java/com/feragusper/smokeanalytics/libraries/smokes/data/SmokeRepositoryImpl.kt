package com.feragusper.smokeanalytics.libraries.smokes.data

import com.feragusper.smokeanalytics.libraries.architecture.domain.firstInstantThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.isThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.isThisWeek
import com.feragusper.smokeanalytics.libraries.architecture.domain.isToday
import com.feragusper.smokeanalytics.libraries.architecture.domain.lastInstantToday
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeAfter
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.SMOKES
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.USERS
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Query.Direction
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmokeRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : SmokeRepository {

    interface FirestoreCollection {
        companion object {
            const val USERS = "users"
            const val SMOKES = "smokes"
        }
    }

    override suspend fun addSmoke(date: Instant) {
        smokesQuery().add(
            SmokeEntity(timestampMillis = date.toEpochMilliseconds().toDouble())
        ).await()
    }

    override suspend fun editSmoke(id: String, date: Instant) {
        smokesQuery()
            .document(id)
            .set(SmokeEntity(timestampMillis = date.toEpochMilliseconds().toDouble()))
            .await()
    }

    override suspend fun deleteSmoke(id: String) {
        smokesQuery()
            .document(id)
            .delete()
            .await()
    }

    override suspend fun fetchSmokes(
        startDate: Instant?,
        endDate: Instant?
    ): List<Smoke> {
        val startMillis = (startDate ?: firstInstantThisMonth()).toEpochMilliseconds().toDouble()
        val endMillis = (endDate ?: lastInstantToday()).toEpochMilliseconds().toDouble()

        var query: Query = smokesQuery()
            .orderBy(SmokeEntity.Fields.TIMESTAMP_MILLIS, Direction.DESCENDING)
            .whereGreaterThanOrEqualTo(SmokeEntity.Fields.TIMESTAMP_MILLIS, startMillis)
            .whereLessThan(SmokeEntity.Fields.TIMESTAMP_MILLIS, endMillis)

        val result = query.get().await()

        val instants = result.documents.mapNotNull { it.getInstant() }

        return result.documents.mapIndexedNotNull { index, document ->
            val currentInstant = instants.getOrNull(index) ?: return@mapIndexedNotNull null
            val previousInstant = instants.getOrNull(index + 1)

            Smoke(
                id = document.id,
                date = currentInstant,
                timeElapsedSincePreviousSmoke = currentInstant.timeAfter(previousInstant)
            )
        }
    }

    override suspend fun fetchSmokeCount(): SmokeCount {
        return fetchSmokes().toSmokeCountListResult()
    }

    private fun List<Smoke>.toSmokeCountListResult() = SmokeCount(
        today = filterToday(),
        week = filterThisWeek().size,
        month = filterThisMonth().size,
        lastSmoke = firstOrNull(),
    )

    private fun List<Smoke>.filterToday() = filter { it.date.isToday() }
    private fun List<Smoke>.filterThisWeek() = filter { it.date.isThisWeek() }
    private fun List<Smoke>.filterThisMonth() = filter { it.date.isThisMonth() }

    private fun smokesQuery() = firebaseAuth.currentUser?.uid?.let { uid ->
        firebaseFirestore.collection("$USERS/$uid/$SMOKES")
    } ?: throw IllegalStateException("User not logged in")

    private fun DocumentSnapshot.getInstant(): Instant? {
        val millis = getDouble(SmokeEntity.Fields.TIMESTAMP_MILLIS) ?: return null
        return Instant.fromEpochMilliseconds(millis.toLong())
    }
}