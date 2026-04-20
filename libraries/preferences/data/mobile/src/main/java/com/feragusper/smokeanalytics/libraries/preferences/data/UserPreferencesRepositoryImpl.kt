package com.feragusper.smokeanalytics.libraries.preferences.data

import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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
        return snapshot.toUserPreferencesEntity()?.toDomain() ?: UserPreferences()
    }

    override suspend fun update(preferences: UserPreferences) {
        document().set(
            UserPreferencesEntity(
                packPrice = preferences.packPrice,
                cigarettesPerPack = preferences.cigarettesPerPack.toLong(),
                dayStartHour = preferences.dayStartHour.toLong(),
                bedtimeHour = preferences.bedtimeHour.toLong(),
                manualDayStartEpochMillis = preferences.manualDayStartEpochMillis,
                locationTrackingEnabled = preferences.locationTrackingEnabled,
                currencySymbol = preferences.currencySymbol,
                accountTier = preferences.accountTier.name,
                activeGoalType = preferences.activeGoal?.type?.name,
                activeGoalMetricValue = preferences.activeGoal?.metricValue,
            )
        ).await()
    }

    private fun document() = firestore.collection("users")
        .document(auth.currentUser?.uid ?: throw IllegalStateException("User not logged in"))
        .collection("profile")
        .document(UserPreferencesEntity.DOCUMENT)
}

private fun DocumentSnapshot.toUserPreferencesEntity(): UserPreferencesEntity? {
    if (!exists()) return null

    return UserPreferencesEntity(
        packPrice = numberOrNull(UserPreferencesEntity.PACK_PRICE)?.toDouble() ?: 0.0,
        cigarettesPerPack = numberOrNull(UserPreferencesEntity.CIGARETTES_PER_PACK)?.toLong() ?: 20,
        dayStartHour = numberOrNull(UserPreferencesEntity.DAY_START_HOUR)?.toLong() ?: 6,
        bedtimeHour = numberOrNull(UserPreferencesEntity.BEDTIME_HOUR)?.toLong() ?: 22,
        manualDayStartEpochMillis = numberOrNull(UserPreferencesEntity.MANUAL_DAY_START_EPOCH_MILLIS)?.toLong(),
        locationTrackingEnabled = booleanOrNull(UserPreferencesEntity.LOCATION_TRACKING_ENABLED) ?: false,
        currencySymbol = stringOrNull(UserPreferencesEntity.CURRENCY_SYMBOL) ?: "€",
        accountTier = stringOrNull(UserPreferencesEntity.ACCOUNT_TIER) ?: "Free",
        activeGoalType = stringOrNull(UserPreferencesEntity.ACTIVE_GOAL_TYPE),
        activeGoalMetricValue = numberOrNull(UserPreferencesEntity.ACTIVE_GOAL_METRIC_VALUE)?.toDouble(),
    )
}

private fun DocumentSnapshot.numberOrNull(field: String): Number? =
    runCatching { getDouble(field) }.getOrNull()
        ?: runCatching { getLong(field) }.getOrNull()
        ?: stringOrNull(field)?.toDoubleOrNull()

private fun DocumentSnapshot.stringOrNull(field: String): String? =
    runCatching { getString(field) }.getOrNull()

private fun DocumentSnapshot.booleanOrNull(field: String): Boolean? =
    runCatching { getBoolean(field) }.getOrNull()
