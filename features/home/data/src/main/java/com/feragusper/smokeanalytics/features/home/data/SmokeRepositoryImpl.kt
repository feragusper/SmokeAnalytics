package com.feragusper.smokeanalytics.features.home.data

import com.feragusper.smokeanalytics.features.home.data.SmokeRepositoryImpl.FirestoreCollection.Companion.SMOKES
import com.feragusper.smokeanalytics.features.home.data.SmokeRepositoryImpl.FirestoreCollection.Companion.USERS
import com.feragusper.smokeanalytics.features.home.domain.Smoke
import com.feragusper.smokeanalytics.features.home.domain.SmokeRepository
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.timeAfter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction
import kotlinx.coroutines.tasks.await
import java.util.Date
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

    override suspend fun addSmoke() {
        val smokes = firebaseAuth.currentUser?.uid?.let {
            firebaseFirestore.collection("$USERS/$it/$SMOKES")
        } ?: throw IllegalStateException("User not logged in")

        smokes.add(SmokeEntity(Date())).await()
    }

    override suspend fun fetchSmokes(): List<Smoke> {
        val smokes = firebaseAuth.currentUser?.uid?.let {
            firebaseFirestore.collection("$USERS/$it/$SMOKES")
                .orderBy(Smoke::date.name, Direction.DESCENDING)
        }?.get()?.await() ?: throw IllegalStateException("User not logged in")

        return smokes.documents.mapIndexedNotNull { index, document ->
            Smoke(
                document.getDate(),
                document.getDate().timeAfter(smokes.documents.getOrNull(index + 1)?.getDate()),
            )
        }
    }

    private fun DocumentSnapshot.getDate() =
        getDate(Smoke::date.name) ?: throw IllegalStateException("Date not found")
}
