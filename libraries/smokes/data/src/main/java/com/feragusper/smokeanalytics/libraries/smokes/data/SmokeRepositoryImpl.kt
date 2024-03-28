package com.feragusper.smokeanalytics.libraries.smokes.data

import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.timeAfter
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.toDate
import com.feragusper.smokeanalytics.libraries.architecture.domain.extensions.toLocalDateTime
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.SMOKES
import com.feragusper.smokeanalytics.libraries.smokes.data.SmokeRepositoryImpl.FirestoreCollection.Companion.USERS
import com.feragusper.smokeanalytics.libraries.smokes.domain.Smoke
import com.feragusper.smokeanalytics.libraries.smokes.domain.SmokeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
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

    override suspend fun addSmoke(date: LocalDateTime) {
        smokes().add(SmokeEntity(date.toDate())).await()
    }

    private fun smokes() = firebaseAuth.currentUser?.uid?.let {
        firebaseFirestore.collection("$USERS/$it/$SMOKES")
    } ?: throw IllegalStateException("User not logged in")

    override suspend fun editSmoke(id: String, date: LocalDateTime) {
        smokes()
            .document(id)
            .set(SmokeEntity(date.toDate()))
            .await()
    }

    override suspend fun deleteSmoke(id: String) {
        smokes()
            .document(id)
            .delete()
            .await()
    }

    override suspend fun fetchSmokes(date: LocalDateTime?): List<Smoke> {
        val smokes = (date?.let {
            smokes()
                .whereGreaterThanOrEqualTo(SmokeEntity::date.name, it.toLocalDate().toDate())
                .whereLessThan(SmokeEntity::date.name, it.plusDays(1).toLocalDate().toDate())
        } ?: smokes())
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

    private fun DocumentSnapshot.getDate() =
        getDate(Smoke::date.name)?.toLocalDateTime()
            ?: throw IllegalStateException("Date not found")
}
