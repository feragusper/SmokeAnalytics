package com.feragusper.smokeanalytics.features.home.data

import com.feragusper.smokeanalytics.features.home.data.SmokeRepositoryImpl.FirestoreCollection.Companion.SMOKES
import com.feragusper.smokeanalytics.features.home.data.SmokeRepositoryImpl.FirestoreCollection.Companion.USERS
import com.feragusper.smokeanalytics.features.home.domain.SmokeRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
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

        smokes.add(Smoke(Timestamp.now())).await()
    }

    private data class Smoke(val date: Timestamp)
}
