package com.feragusper.smokeanalytics.libraries.preferences.data

import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferences
import com.feragusper.smokeanalytics.libraries.preferences.domain.UserPreferencesRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

class UserPreferencesRepositoryImpl(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth,
) : UserPreferencesRepository {

    override suspend fun fetch(): UserPreferences {
        val snapshot = document().get()
        return snapshot.toUserPreferencesEntity()?.toDomain() ?: UserPreferences()
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

private fun DocumentSnapshot.toUserPreferencesEntity(): UserPreferencesEntity? {
    if (!exists) return null

    return UserPreferencesEntity(
        packPrice = numberOrNull(UserPreferencesEntity.PACK_PRICE)?.toDouble() ?: 0.0,
        cigarettesPerPack = numberOrNull(UserPreferencesEntity.CIGARETTES_PER_PACK)?.toInt() ?: 20,
        dayStartHour = numberOrNull(UserPreferencesEntity.DAY_START_HOUR)?.toInt() ?: 6,
        bedtimeHour = numberOrNull(UserPreferencesEntity.BEDTIME_HOUR)?.toInt() ?: 22,
        manualDayStartEpochMillis = numberOrNull(UserPreferencesEntity.MANUAL_DAY_START_EPOCH_MILLIS)?.toLong(),
        locationTrackingEnabled = getOrNull<Boolean>(UserPreferencesEntity.LOCATION_TRACKING_ENABLED) ?: false,
        currencySymbol = getOrNull<String>(UserPreferencesEntity.CURRENCY_SYMBOL) ?: "€",
        accountTier = getOrNull<String>(UserPreferencesEntity.ACCOUNT_TIER) ?: "Free",
        activeGoalType = getOrNull<String>(UserPreferencesEntity.ACTIVE_GOAL_TYPE),
        activeGoalMetricValue = numberOrNull(UserPreferencesEntity.ACTIVE_GOAL_METRIC_VALUE)?.toDouble(),
    )
}

private fun DocumentSnapshot.numberOrNull(field: String): Number? =
    getOrNull<Double>(field)
        ?: getOrNull<Long>(field)
        ?: getOrNull<Int>(field)
        ?: getOrNull<String>(field)?.toDoubleOrNull()

private inline fun <reified T> DocumentSnapshot.getOrNull(field: String): T? =
    try {
        get(field)
    } catch (_: Throwable) {
        null
    }
