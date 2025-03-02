package com.feragusper.smokeanalytics.libraries.smokes.data

import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isThisWeek
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.isToday
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeAfter
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.toDate
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.toLocalDateTime
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
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [SmokeRepository] that interacts with Firebase Firestore
 * to perform CRUD operations on smoke data for the authenticated user.
 *
 * @property firebaseFirestore The instance of [FirebaseFirestore] for database operations.
 * @property firebaseAuth The instance of [FirebaseAuth] for authentication details.
 */
@Singleton
class SmokeRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : SmokeRepository {

    /**
     * Constants for Firestore collection paths.
     */
    interface FirestoreCollection {
        companion object {
            const val USERS = "users"
            const val SMOKES = "smokes"
        }
    }

    /**
     * Adds a new smoke event to Firestore for the current user.
     *
     * @param date The date and time of the smoke event.
     */
    override suspend fun addSmoke(date: LocalDateTime) {
        smokesQuery().add(SmokeEntity(date.toDate())).await()
    }

    /**
     * Edits an existing smoke event in Firestore.
     *
     * @param id The ID of the smoke event to edit.
     * @param date The new date and time for the smoke event.
     */
    override suspend fun editSmoke(id: String, date: LocalDateTime) {
        smokesQuery()
            .document(id)
            .set(SmokeEntity(date.toDate()))
            .await()
    }

    /**
     * Deletes a smoke event from Firestore.
     *
     * @param id The ID of the smoke event to delete.
     */
    override suspend fun deleteSmoke(id: String) {
        smokesQuery()
            .document(id)
            .delete()
            .await()
    }

    /**
     * Fetches the list of smoke events for the current user, optionally filtered by date range.
     *
     * @param startDate The start date for filtering smoke events (inclusive).
     * @param endDate The end date for filtering smoke events (exclusive).
     * @return A list of [Smoke] objects.
     */
    override suspend fun fetchSmokes(
        startDate: LocalDateTime?,
        endDate: LocalDateTime?
    ): List<Smoke> {
        val baseQuery = smokesQuery()

        var query: Query = baseQuery.orderBy(SmokeEntity::date.name, Direction.DESCENDING)

        startDate?.let {
            query = query.whereGreaterThanOrEqualTo(SmokeEntity::date.name, it.toDate())
        }
        endDate?.let {
            query = query.whereLessThan(SmokeEntity::date.name, it.toDate())
        }

        val result = query.get().await()

        return result.documents.mapIndexedNotNull { index, document ->
            val currentDate = document.getDate()
            val previousDate = result.documents.getOrNull(index + 1)?.getDate()

            // Calculate time elapsed since the previous smoke event
            val timeElapsedSincePreviousSmoke = if (previousDate != null) {
                currentDate.timeAfter(previousDate)
            } else {
                Pair(0L, 0L)  // Handle first smoke event case
            }

            Smoke(
                id = document.id,
                date = currentDate,
                timeElapsedSincePreviousSmoke = timeElapsedSincePreviousSmoke
            )
        }
    }

    /**
     * Fetches the smoke count statistics for the current user.
     *
     * @return A [SmokeCount] object containing counts for today, week, and month.
     */
    override suspend fun fetchSmokeCount(): SmokeCount {
        return fetchSmokes().toSmokeCountListResult()
    }

    /**
     * Maps a list of [Smoke] objects to a [SmokeCount] object.
     *
     * @return A [SmokeCount] containing the counts for today, week, and month.
     */
    private fun List<Smoke>.toSmokeCountListResult() = SmokeCount(
        today = filterToday(),
        week = filterThisWeek().size,
        month = filterThisMonth().size,
        lastSmoke = firstOrNull(),
    )

    /**
     * Filters the smoke events for today.
     */
    private fun List<Smoke>.filterToday() = filter { it.date.isToday() }

    /**
     * Filters the smoke events for the current week.
     */
    private fun List<Smoke>.filterThisWeek() = filter { it.date.isThisWeek() }

    /**
     * Filters the smoke events for the current month.
     */
    private fun List<Smoke>.filterThisMonth() = filter { it.date.isThisMonth() }

    /**
     * Helper method to retrieve the Firestore collection reference for the current user's smokes.
     *
     * @throws IllegalStateException if the user is not logged in.
     * @return The Firestore collection reference.
     */
    private fun smokesQuery() = firebaseAuth.currentUser?.uid?.let {
        firebaseFirestore.collection("$USERS/$it/$SMOKES")
    } ?: throw IllegalStateException("User not logged in")

    /**
     * Extension function to convert a Firestore [DocumentSnapshot] to a [LocalDateTime].
     *
     * @throws IllegalStateException if the date is not found in the document.
     * @return The [LocalDateTime] representation of the date.
     */
    private fun DocumentSnapshot.getDate() =
        getDate(Smoke::date.name)?.toLocalDateTime()
            ?: throw IllegalStateException("Date not found")
}
