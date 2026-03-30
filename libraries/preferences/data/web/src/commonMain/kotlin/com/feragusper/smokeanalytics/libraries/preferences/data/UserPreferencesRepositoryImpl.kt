package com.feragusper.smokeanalytics.libraries.preferences.data

import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

class UserPreferencesRepositoryImpl(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth,
) : UserPreferencesRepository {

    override suspend fun fetch(): UserPreferences {
        val snapshot = document().get()
        return snapshot.data<UserPreferencesEntity>()?.toDomain() ?: UserPreferences()
    }

    override suspend fun update(preferences: UserPreferences) {
        document().set(
            UserPreferencesEntity(
                packPrice = preferences.packPrice,
                cigarettesPerPack = preferences.cigarettesPerPack,
                dayStartHour = preferences.dayStartHour,
                bedtimeHour = preferences.bedtimeHour,
                manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                locationTrackingEnabled = preferences.locationTrackingEnabled,
                currencySymbol = preferences.currencySymbol,
                accountTier = preferences.accountTier.name,
                activeGoalType = preferences.activeGoal?.type?.name,
                activeGoalMetricValue = preferences.activeGoal?.metricValue,
            )
        )
    }

    private fun document() = firestore.collection("users")
        .document(auth.currentUser?.uid ?: throw IllegalStateException("User not logged in"))
        .collection("profile")
        .document(UserPreferencesEntity.DOCUMENT)
}
