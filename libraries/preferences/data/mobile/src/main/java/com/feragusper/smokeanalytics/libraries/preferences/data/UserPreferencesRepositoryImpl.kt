package com.feragusper.smokeanalytics.libraries.preferences.data

import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : UserPreferencesRepository {

    override suspend fun fetch(): UserPreferences {
        val snapshot = document().get().await()
        return snapshot.toObject(UserPreferencesEntity::class.java)?.toDomain() ?: UserPreferences()
    }

    override suspend fun update(preferences: UserPreferences) {
        document().set(
            UserPreferencesEntity(
                packPrice = preferences.packPrice,
                cigarettesPerPack = preferences.cigarettesPerPack.toLong(),
                dayStartHour = preferences.dayStartHour.toLong(),
                manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                locationTrackingEnabled = preferences.locationTrackingEnabled,
                currencySymbol = preferences.currencySymbol,
                accountTier = preferences.accountTier.name,
            )
        ).await()
    }

    private fun document() = firestore.collection("users")
        .document(auth.currentUser?.uid ?: throw IllegalStateException("User not logged in"))
        .collection("profile")
        .document(UserPreferencesEntity.DOCUMENT)
}
