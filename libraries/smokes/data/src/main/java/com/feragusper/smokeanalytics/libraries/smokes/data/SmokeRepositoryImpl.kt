package com.feragusper.smokeanalytics.libraries.smokes.data

import android.content.Context
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.firstInstantThisMonth
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.lastInstantToday
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeAfter
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.toDate
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.toLocalDateTime
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.SMOKES
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.USERS
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.SmokeRepository
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths
import com.feragusper.smokeanalytics.libraries.wear.data.WearPaths.SMOKE_COUNT
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [SmokeRepository] that interacts with Firebase Firestore to perform CRUD operations
 * on smoke data for the authenticated user.
 *
 * @property firebaseFirestore The instance of [FirebaseFirestore] for database operations.
 * @property firebaseAuth The instance of [FirebaseAuth] for authentication details.
 */
@Singleton
class SmokeRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context,
) : SmokeRepository, OnMessageReceivedListener, CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.IO + job

    /**
     * Constants for Firestore collection paths.
     */
    interface FirestoreCollection {
        companion object {
            const val USERS = "users"
            const val SMOKES = "smokes"
        }
    }

    override suspend fun addSmoke(date: LocalDateTime) {
        smokesQuery().add(SmokeEntity(date.toDate())).await()

        syncWithWear()
    }

    override suspend fun editSmoke(id: String, date: LocalDateTime) {
        smokesQuery()
            .document(id)
            .set(SmokeEntity(date.toDate()))
            .await()

        syncWithWear()
    }

    override suspend fun deleteSmoke(id: String) {
        smokesQuery()
            .document(id)
            .delete()
            .await()

        syncWithWear()
    }

    override suspend fun fetchSmokes(date: LocalDateTime?): List<Smoke> {
        val smokes = smokesQuery()
            .whereGreaterThanOrEqualTo(
                SmokeEntity::date.name,
                (date?.toLocalDate()?.atStartOfDay() ?: firstInstantThisMonth()).toDate()
            )
            .whereLessThan(
                SmokeEntity::date.name,
                (date?.plusDays(1)?.toLocalDate()?.atStartOfDay() ?: lastInstantToday()).toDate()
            )

            .orderBy(Smoke::date.name, Direction.DESCENDING)
            .get()
            .await()

        return smokes.documents.mapIndexedNotNull { index, document ->
            Smoke(
                id = document.id,
                date = document.getDate(),
                timeElapsedSincePreviousSmoke = document.getDate()
                    .timeAfter(smokes.documents.getOrNull(index + 1)?.getDate()),
            )
        }
    }

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

    private suspend fun syncWithWear() {
        respondToWearWithSmokeCount(fetchSmokes().size)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Timber.d("onMessageReceived: ${messageEvent.path}")
        if (messageEvent.path == WearPaths.REQUEST_SMOKES) {
            launch { syncWithWear() }
        }
    }

    private fun respondToWearWithSmokeCount(smokeCount: Int) {
        Timber.d("respondToWearWithSmokeCount: $smokeCount")
        val putDataMapRequest = PutDataMapRequest.create(WearPaths.SMOKE_DATA).apply {
            dataMap.putInt(SMOKE_COUNT, smokeCount)
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(putDataRequest)
    }
}
