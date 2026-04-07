package com.feragusper.smokeanalytics.libraries.smokes.data

import com.feragusper.smokeanalytics.libraries.architecture.domain.firstInstantThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentMonthStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.currentWeekStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.isInCurrentDayBucket
import com.feragusper.smokeanalytics.libraries.architecture.domain.isInCurrentMonthBucket
import com.feragusper.smokeanalytics.libraries.architecture.domain.isInCurrentWeekBucket
import com.feragusper.smokeanalytics.libraries.architecture.domain.nextDayStartInstant
import com.feragusper.smokeanalytics.libraries.architecture.domain.timeAfter
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.SMOKES
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.USERS
import com.feragusper.smokeanalytics.libraries.smokes.domain.model.GeoPoint
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

    override suspend fun addSmoke(date: Instant, location: GeoPoint?) {
        smokesQuery().add(
            SmokeEntity(
                timestampMillis = date.toEpochMilliseconds().toDouble(),
                latitude = location?.latitude,
                longitude = location?.longitude,
            )
        ).await()
    }

    override suspend fun editSmoke(id: String, date: Instant, location: GeoPoint?) {
        val preservedLocation = location ?: smokesQuery()
            .document(id)
            .get()
            .await()
            .getGeoPoint()

        smokesQuery()
            .document(id)
            .set(
                SmokeEntity(
                    timestampMillis = date.toEpochMilliseconds().toDouble(),
                    latitude = preservedLocation?.latitude,
                    longitude = preservedLocation?.longitude,
                )
            )
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
        val endMillis = (endDate ?: nextDayStartInstant()).toEpochMilliseconds().toDouble()

        val query: Query = smokesQuery()
            .orderBy(SmokeEntity.Fields.TIMESTAMP_MILLIS, Direction.DESCENDING)
            .whereGreaterThanOrEqualTo(SmokeEntity.Fields.TIMESTAMP_MILLIS, startMillis)
            .whereLessThan(SmokeEntity.Fields.TIMESTAMP_MILLIS, endMillis)

        val result = query.get().await()
        val previousDocument = smokesQuery()
            .orderBy(SmokeEntity.Fields.TIMESTAMP_MILLIS, Direction.DESCENDING)
            .whereLessThan(SmokeEntity.Fields.TIMESTAMP_MILLIS, startMillis)
            .limit(1)
            .get()
            .await()
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

    override suspend fun fetchSmokeCount(dayStartHour: Int, manualDayStartEpochMillis: Long?): SmokeCount {
        val monthStart = currentMonthStartInstant(
            dayStartHour = dayStartHour,
            manualDayStartEpochMillis = manualDayStartEpochMillis,
        )
        val weekStart = currentWeekStartInstant(
            dayStartHour = dayStartHour,
            manualDayStartEpochMillis = manualDayStartEpochMillis,
        )
        return fetchSmokes(
            startDate = if (weekStart < monthStart) weekStart else monthStart,
            endDate = nextDayStartInstant(
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

    private fun smokesQuery() = firebaseAuth.currentUser?.uid?.let { uid ->
        firebaseFirestore.collection("$USERS/$uid/$SMOKES")
    } ?: throw IllegalStateException("User not logged in")

    private fun DocumentSnapshot.getInstant(): Instant? {
        val millis = getDouble(SmokeEntity.Fields.TIMESTAMP_MILLIS) ?: return null
        return Instant.fromEpochMilliseconds(millis.toLong())
    }

    private fun DocumentSnapshot.getGeoPoint(): GeoPoint? {
        val latitude = getDouble(SmokeEntity.Fields.LATITUDE) ?: return null
        val longitude = getDouble(SmokeEntity.Fields.LONGITUDE) ?: return null
        return GeoPoint(latitude = latitude, longitude = longitude)
    }
}
