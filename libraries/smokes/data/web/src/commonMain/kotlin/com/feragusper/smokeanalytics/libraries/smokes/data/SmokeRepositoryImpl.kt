package com.feragusper.smokeanalytics.libraries.smokes.data

import com.feragusper.smokeanalytics.libraries.architecture.domain.firstInstantThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.isInCurrentDayBucket
import com.feragusper.smokeanalytics.libraries.architecture.domain.isInCurrentMonthBucket
import com.feragusper.smokeanalytics.libraries.architecture.domain.isInCurrentWeekBucket
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeAfter
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
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
    override suspend fun addSmoke(date: Instant, location: GeoPoint?) {
        smokesCollection().add(
            SmokeEntity(
                timestampMillis = date.toEpochMilliseconds().toDouble(),
                latitude = location?.latitude,
                longitude = location?.longitude,
            )
        )
    }

    /**
     * @see SmokeRepository.editSmoke
     */
    override suspend fun editSmoke(id: String, date: Instant, location: GeoPoint?) {
        val preservedLocation = location ?: smokesCollection()
            .document(id)
            .get()
            .getGeoPoint()

        smokesCollection()
            .document(id)
            .set(
                SmokeEntity(
                    timestampMillis = date.toEpochMilliseconds().toDouble(),
                    latitude = preservedLocation?.latitude,
                    longitude = preservedLocation?.longitude,
                )
            )
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
        val endMillis = (end ?: nextDayStartInstant()).toEpochMilliseconds().toDouble()

        val result = smokesCollection()
            .orderBy(SmokeEntity.Fields.TIMESTAMP_MILLIS, Direction.DESCENDING)
            .where {
                SmokeEntity.Fields.TIMESTAMP_MILLIS greaterThanOrEqualTo startMillis
            }
            .where {
                SmokeEntity.Fields.TIMESTAMP_MILLIS lessThan endMillis
            }
            .get()

        val previousDocument = smokesCollection()
            .orderBy(SmokeEntity.Fields.TIMESTAMP_MILLIS, Direction.DESCENDING)
            .where {
                SmokeEntity.Fields.TIMESTAMP_MILLIS lessThan startMillis
            }
            .limit(1)
            .get()
            .documents
            .firstOrNull()
        val documents = previousDocument?.let { result.documents + it } ?: result.documents
        val instants = documents.mapNotNull { it.getInstant() }

        return result.documents.mapIndexedNotNull { index, document ->
            val currentInstant = instants.getOrNull(index) ?: return@mapIndexedNotNull null
            val previousInstant = instants.getOrNull(index + 1)

            Smoke(
                id = document.id,
                date = currentInstant,
                timeElapsedSincePreviousSmoke = currentInstant.timeAfter(previousInstant),
                location = document.getGeoPoint(),
            )
        }
    }

    /**
     * @see SmokeRepository.fetchSmokeCount
     */
    override suspend fun fetchSmokeCount(dayStartHour: Int, manualDayStartEpochMillis: Long?): SmokeCount {
        return fetchSmokes(
            start = firstInstantThisMonth(dayStartHour = dayStartHour),
            end = nextDayStartInstant(
                dayStartHour = dayStartHour,
                manualDayStartEpochMillis = manualDayStartEpochMillis,
            ),
        ).toSmokeCountListResult(dayStartHour, manualDayStartEpochMillis)
    }

    private fun List<Smoke>.toSmokeCountListResult(dayStartHour: Int, manualDayStartEpochMillis: Long?) = SmokeCount(
        today = filterToday(dayStartHour, manualDayStartEpochMillis),
        week = filterThisWeek(dayStartHour, manualDayStartEpochMillis).size,
        month = filterThisMonth(dayStartHour, manualDayStartEpochMillis).size,
        lastSmoke = firstOrNull(),
    )

    private fun List<Smoke>.filterToday(dayStartHour: Int, manualDayStartEpochMillis: Long?) =
        filter { it.date.isInCurrentDayBucket(dayStartHour = dayStartHour, manualDayStartEpochMillis = manualDayStartEpochMillis) }

    private fun List<Smoke>.filterThisWeek(dayStartHour: Int, manualDayStartEpochMillis: Long?) =
        filter { it.date.isInCurrentWeekBucket(dayStartHour = dayStartHour, manualDayStartEpochMillis = manualDayStartEpochMillis) }

    private fun List<Smoke>.filterThisMonth(dayStartHour: Int, manualDayStartEpochMillis: Long?) =
        filter { it.date.isInCurrentMonthBucket(dayStartHour = dayStartHour, manualDayStartEpochMillis = manualDayStartEpochMillis) }

    private fun smokesCollection() = firebaseAuth.currentUser?.uid?.let { uid ->
        firebaseFirestore.collection("${FirestoreCollection.USERS}/$uid/${FirestoreCollection.SMOKES}")
    } ?: throw IllegalStateException("User not logged in")

    private fun DocumentSnapshot.getInstant(): Instant? {
        val millis = getOrNull<Double>(SmokeEntity.Fields.TIMESTAMP_MILLIS) ?: return null
        return Instant.fromEpochMilliseconds(millis.toLong())
    }

    private fun DocumentSnapshot.getGeoPoint(): GeoPoint? {
        val latitude = getOrNull<Double>(SmokeEntity.Fields.LATITUDE) ?: return null
        val longitude = getOrNull<Double>(SmokeEntity.Fields.LONGITUDE) ?: return null
        return GeoPoint(latitude = latitude, longitude = longitude)
    }

    private inline fun <reified T> DocumentSnapshot.getOrNull(field: String): T? =
        try {
            get(field)
        } catch (_: Throwable) {
            null
        }
}
