package com.feragusper.smokeanalytics.libraries.smokes.data

import com.feragusper.smokeanalytics.libraries.architecture.domain.firstInstantThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.isThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.isThisWeek
import com.feragusper.smokeanalytics.libraries.architecture.domain.isToday
import com.feragusper.smokeanalytics.libraries.architecture.domain.lastInstantToday
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeAfter
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.SmokeCount
import com.feragusper.smokeanalytics.libraries.smokes.domain.repository.SmokeRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.datetime.Instant

/**
 * Represents a smoke repository.
 *
 * @property firebaseFirestore The Firebase Firestore.
 * @property firebaseAuth The Firebase Auth.
 */
class SmokeRepositoryImpl(
    private val firebaseFirestore: FirebaseFirestore = Firebase.firestore,
    private val firebaseAuth: FirebaseAuth = Firebase.auth,
) : SmokeRepository {

    /**
     * Represents the Firestore collection.
     */
    interface FirestoreCollection {
        companion object {
            const val USERS = "users"
            const val SMOKES = "smokes"
        }
    }

    /**
     * @see SmokeRepository.addSmoke
     */
    override suspend fun addSmoke(date: Instant) {
        smokesCollection().add(
            SmokeEntity(timestampMillis = date.toEpochMilliseconds().toDouble())
        )
    }

    /**
     * @see SmokeRepository.editSmoke
     */
    override suspend fun editSmoke(id: String, date: Instant) {
        smokesCollection()
            .document(id)
            .set(SmokeEntity(timestampMillis = date.toEpochMilliseconds().toDouble()))
    }

    /**
     * @see SmokeRepository.deleteSmoke
     */
    override suspend fun deleteSmoke(id: String) {
        smokesCollection()
            .document(id)
            .delete()
    }

    /**
     * @see SmokeRepository.fetchSmokes
     */
    override suspend fun fetchSmokes(
        start: Instant?,
        end: Instant?,
    ): List<Smoke> {
        val startMillis = (start ?: firstInstantThisMonth()).toEpochMilliseconds().toDouble()
        val endMillis = (end ?: lastInstantToday()).toEpochMilliseconds().toDouble()

        val result = smokesCollection()
            .orderBy(SmokeEntity.Fields.TIMESTAMP_MILLIS, Direction.DESCENDING)
            .where {
                SmokeEntity.Fields.TIMESTAMP_MILLIS greaterThanOrEqualTo startMillis
            }
            .where {
                SmokeEntity.Fields.TIMESTAMP_MILLIS lessThan endMillis
            }
            .get()

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

    /**
     * @see SmokeRepository.fetchSmokeCount
     */
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

    private fun smokesCollection() = firebaseAuth.currentUser?.uid?.let { uid ->
        firebaseFirestore.collection("${FirestoreCollection.USERS}/$uid/${FirestoreCollection.SMOKES}")
    } ?: throw IllegalStateException("User not logged in")

    private fun DocumentSnapshot.getInstant(): Instant? {
        val millis = getOrNull<Double>(SmokeEntity.Fields.TIMESTAMP_MILLIS) ?: return null
        return Instant.fromEpochMilliseconds(millis.toLong())
    }

    private inline fun <reified T> DocumentSnapshot.getOrNull(field: String): T? =
        try {
            get(field)
        } catch (_: Throwable) {
            null
        }
}